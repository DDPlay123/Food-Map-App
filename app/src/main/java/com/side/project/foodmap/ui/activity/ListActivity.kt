package com.side.project.foodmap.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ActivityListBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.getStatusBarHeight
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.adapter.RestaurantListAdapter
import com.side.project.foodmap.ui.viewModel.ListViewModel
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource

class ListActivity : BaseActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: ListViewModel

    // Data
    private lateinit var keyword: String
    private var latitude = DEFAULT_LATITUDE
    private var longitude = DEFAULT_LONGITUDE
    // Tool
    private lateinit var restaurantListAdapter: RestaurantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list)
        viewModel = ViewModelProvider(this)[ListViewModel::class.java]
        binding.paddingTop = getStatusBarHeight()

        checkNetWork { onBackPressed() }

        getArguments()
        initLocationService()
        doInitialize()
        setListener()
    }

    private fun getArguments() {
        intent.extras?.let {
            keyword = it.getString("TITLE", "") ?: ""
            latitude = it.getDouble("LATITUDE", 0.0)
            longitude = it.getDouble("LONGITUDE", 0.0)
        }
    }

    private fun doInitialize() {
        // 初始化
        if (::keyword.isInitialized)
            viewModel.nearSearch(keyword, LatLng(latitude, longitude))

        binding.title = keyword

        // 附近搜尋
        lifecycleScope.launchWhenCreated {
            viewModel.nearSearchState.collect {
                when (it) {
                    is Resource.Loading -> {
                        Method.logE("Near Search", "Loading")
                        dialog.showLoadingDialog(false)
                    }
                    is Resource.Success -> {
                        Method.logE("Near Search", "Success")
                        it.data?.let { data -> viewModel.insertDistanceSearchData(data) }
                    }
                    is Resource.Error -> {
                        Method.logE("Near Search", "Error:${it.message.toString()}")
                        dialog.cancelLoadingDialog()
                        displayShortToast(getString(R.string.hint_error))
                        viewModel.getDistanceSearchData()
                    }
                    else -> Unit
                }
            }
        }

        // 附近搜尋 From Room
        lifecycleScope.launchWhenCreated {
            viewModel.getDistanceSearch.collect {
                when (it) {
                    is Resource.Loading -> {
                        Method.logE("Near Search Room", "Loading")
                        dialog.showLoadingDialog(false)
                    }
                    is Resource.Success -> {
                        Method.logE("Near Search Room", "Success")
                        dialog.cancelLoadingDialog()
                        it.data?.let { data ->
                            binding.count = data.result.placeCount.toString()
                            initRestaurant(data.result.placeList)
                        }
                    }
                    is Resource.Error -> {
                        Method.logE("Near Search Room", "Error:${it.message.toString()}")
                        dialog.cancelLoadingDialog()
                        displayShortToast(getString(R.string.hint_error))
                    }
                    else -> Unit
                }
            }
        }

        // 查看詳細資料
        lifecycleScope.launchWhenCreated {
            viewModel.watchDetailState.collect {
                when (it) {
                    is Resource.Success -> {
                        Method.logE("Watch Detail", "Success")
                        Bundle().also { b ->
                            b.putString("PLACE_ID", it.data.toString())
                            mActivity.start(DetailActivity::class.java, b)
                            viewModel._watchDetailState.emit(Resource.Loading())
                        }
                    }
                    is Resource.Error -> {
                        Method.logE("Watch Detail", "Error:${it.message.toString()}")
                        displayShortToast(getString(R.string.hint_error))
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setListener() {
        binding.run {
            imgBack.setOnClickListener { onBackPressed() }
        }
    }

    private fun initRestaurant(placeList: ArrayList<PlaceList>) {
        restaurantListAdapter = RestaurantListAdapter()
        binding.rvRestaurants.apply {
            layoutManager = GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)
            adapter = restaurantListAdapter
            restaurantListAdapter.setterData(placeList)
        }

        restaurantListAdapter.onItemClick = { viewModel.watchDetail(it) }
    }
}