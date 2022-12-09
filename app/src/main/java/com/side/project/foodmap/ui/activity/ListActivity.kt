package com.side.project.foodmap.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ActivityListBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.adapter.RestaurantListAdapter
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.viewModel.ListViewModel
import com.side.project.foodmap.util.Constants.IS_NEAR_SEARCH
import com.side.project.foodmap.util.Constants.KEYWORD
import com.side.project.foodmap.util.Constants.LATITUDE
import com.side.project.foodmap.util.Constants.LONGITUDE
import com.side.project.foodmap.util.Constants.PLACE_ID
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ListActivity : BaseActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: ListViewModel
    private val animManager: AnimManager by inject()

    // Data
    private lateinit var keyword: String
    private var isNearSearch: Boolean = true
    private var latitude = DEFAULT_LATITUDE
    private var longitude = DEFAULT_LONGITUDE
    // Tool
    private lateinit var restaurantListAdapter: RestaurantListAdapter

    // Parameter
    private var alreadyCalledNum: Long = 0
    private var repeatNum: Long = 0
    private var totalCount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list)
        viewModel = ViewModelProvider(this)[ListViewModel::class.java]
        binding.paddingTop = getStatusBarHeight()

        checkNetWork { onBackPressed() }

        getArguments()
        initLocationService()
        doInitialize()
        initRvRestaurant()
        setListener()
    }

    private fun getArguments() {
        intent.extras?.let {
            keyword = it.getString(KEYWORD, "") ?: ""
            isNearSearch = it.getBoolean(IS_NEAR_SEARCH, true)
            latitude = it.getDouble(LATITUDE, 0.0)
            longitude = it.getDouble(LONGITUDE, 0.0)
        }
    }

    private fun doInitialize() {
        // 初始化
        if (!::keyword.isInitialized) return

        if (isNearSearch)
            viewModel.nearSearch(keyword, LatLng(latitude, longitude), distance = 5000, skip = 0, limit = 50)
        else
            viewModel.keywordSearch()

        binding.title = keyword

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 附近搜尋
                launch {
                    viewModel.nearSearchState.observe(this@ListActivity) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                Method.logE("Near Search", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                            }
                            is Resource.Success -> {
                                Method.logE("Near Search", "Success")
                                dialog.cancelLoadingDialog()
                                resource.data?.let { data ->
                                    totalCount = data.result.placeCount
                                    repeatNum = totalCount / 50
                                }
                                return@observe
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
                // 觀察 List 資料變化
                launch {
                    viewModel.observeSearchData.observe(this@ListActivity) { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let { placeList ->
                                    binding.count = placeList.size.toString()
                                    restaurantListAdapter.setData(placeList.toMutableList())
                                    restaurantListAdapter.setMyLocation(LatLng(myLatitude, myLongitude))
                                }
                            }
                            is Resource.Error -> {
                                displayShortToast(getString(R.string.hint_no_more_data))
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

            fabUpTool.setOnClickListener {
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(this@ListActivity) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }
                smoothScroller.targetPosition = 0
                rvRestaurants.layoutManager?.startSmoothScroll(smoothScroller)
            }
        }
    }

    private fun initRvRestaurant() {
        restaurantListAdapter = RestaurantListAdapter()
        binding.rvRestaurants.apply {
            layoutManager = GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)
            adapter = restaurantListAdapter
            setRvItemListener()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstItemPosition: Int = (binding.rvRestaurants.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

                    if (firstItemPosition >= 1)
                        binding.fabUpTool.display()
                    else
                        binding.fabUpTool.gone()

                    if (!canScrollVertically(1)) {
                        // 判斷第幾次呼叫
                        if (alreadyCalledNum < repeatNum)
                            alreadyCalledNum++

                        if (isNearSearch)
                            viewModel.nearSearch(keyword, LatLng(latitude, longitude), distance = 5000, skip = (50 * alreadyCalledNum).toInt(), limit = 50)
                        else
                            viewModel.keywordSearch()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // 不滾動時
                    if (newState == RecyclerView.SCROLL_STATE_IDLE)
                        binding.fabUpTool.delayOnLifecycle(1500L) {
                            binding.fabUpTool.gone()
                        }
                }
            })
        }
    }

    private fun setRvItemListener() {
        restaurantListAdapter.onItemClick = { placeId ->
            try {
                Method.logE("Watch Detail", "Success")
                Bundle().also { b ->
                    b.putString(PLACE_ID, placeId)
                    mActivity.start(DetailActivity::class.java, b)
                }
            } catch (e: Exception) {
                Method.logE("Watch Detail", "Error")
                displayShortToast(getString(R.string.hint_error))
            }
        }
    }
}