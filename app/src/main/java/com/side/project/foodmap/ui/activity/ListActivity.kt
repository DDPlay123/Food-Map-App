package com.side.project.foodmap.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ActivityListBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.adapter.RestaurantListAdapter
import com.side.project.foodmap.ui.viewModel.ListViewModel
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListActivity : BaseActivity() {
    private lateinit var binding: ActivityListBinding
    private val viewModel: ListViewModel by viewModel()

    // Data
    private var isLocal: Boolean = true
    private lateinit var title: String
    private lateinit var count: String
    // Tool
    private lateinit var restaurantListAdapter: RestaurantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list)
        binding.paddingTop = getStatusBarHeight()

        checkNetWork { onBackPressed() }

        getArguments()
        doInitialize()
        setListener()
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0)
            result = resources.getDimensionPixelSize(resourceId)

        return result
    }

    private fun getArguments() {
        intent.extras?.let {
            title = it.getString("TITLE", "") ?: ""
            isLocal = it.getBoolean("IS_LOCAL", true)
        }
    }

    private fun doInitialize() {
        // 初始化
        if (isLocal && ::title.isInitialized)
            viewModel.getDistanceSearchData()
        else
            viewModel.keywordSearch()

        binding.title = title

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