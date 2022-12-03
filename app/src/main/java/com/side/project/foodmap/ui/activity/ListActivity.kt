package com.side.project.foodmap.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.launch
import java.lang.Exception

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 附近搜尋
                launch {
                    viewModel.nearSearchState.observe(this@ListActivity) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                Method.logE("Near Search", "Loading")
                                dialog.showLoadingDialog(false)
                            }
                            is Resource.Success -> {
                                Method.logE("Near Search", "Success")
                            }
                            is Resource.Error -> {
                                Method.logE("Near Search", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                displayShortToast(getString(R.string.hint_error))
                                viewModel.getDistanceSearchData()
                            }
                            else -> Unit
                        }
                    }
                }
                // 附近搜尋 From Room
                launch {
                    viewModel.getDistanceSearch.observe(this@ListActivity) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                Method.logE("Near Search Room", "Loading")
                                dialog.showLoadingDialog(false)
                                return@observe
                            }
                            is Resource.Success -> {
                                Method.logE("Near Search Room", "Success")
                                dialog.cancelLoadingDialog()
                                resource.data?.let { data ->
                                    binding.count = data.result.placeCount.toString()
                                    initRestaurant(data.result.placeList)
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                Method.logE("Near Search Room", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                displayShortToast(getString(R.string.hint_error))
                                return@observe
                            }
                            else -> Unit
                        }
                    }
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

        restaurantListAdapter.onItemClick = {
            try {
                Method.logE("Watch Detail", "Success")
                Bundle().also { b ->
                    b.putString("PLACE_ID", it)
                    mActivity.start(DetailActivity::class.java, b)
                }
            } catch (e: Exception) {
                Method.logE("Watch Detail", "Error")
                displayShortToast(getString(R.string.hint_error))
            }
        }
    }
}