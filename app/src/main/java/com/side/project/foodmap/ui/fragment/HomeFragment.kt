package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.google.placesSearch.PlacesSearch
import com.side.project.foodmap.data.remote.google.placesSearch.Result
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.helper.appInfo
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.service.LocationService
import com.side.project.foodmap.ui.adapter.QuickViewAdapter
import com.side.project.foodmap.ui.adapter.RegionSelectAdapter
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.viewModel.HomeViewModel
import com.side.project.foodmap.util.Constants.PERMISSION_COARSE_LOCATION
import com.side.project.foodmap.util.Constants.PERMISSION_FINE_LOCATION
import com.side.project.foodmap.util.Method.logE
import com.side.project.foodmap.util.Method.requestPermission
import com.side.project.foodmap.util.Resource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList
import kotlin.math.abs

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: HomeViewModel by viewModel()
    private val animManager: AnimManager by inject()

    private lateinit var locationService: LocationService
    private var myLatitude: Double = DEFAULT_LATITUDE
    private var myLongitude: Double = DEFAULT_LONGITUDE

    private lateinit var regionList: ArrayList<String>
    private var regionID: Int = 0

    private lateinit var quickViewAdapter: QuickViewAdapter

    override fun FragmentHomeBinding.initialize() {
        binding.vm = viewModel
        regionList = ArrayList(listOf(*resources.getStringArray(R.array.search_type)))

        val permission = arrayOf(PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION)
        requestPermission(mActivity, *permission)
        initLocationService()

//        checkTdxToken() // 暫時棄用 TDX API
    }

    private fun initLocationService() {
        locationService = LocationService()
        locationService.startListener(mActivity)
        if (!locationService.canGetLocation()) {
            mActivity.displayShortToast(getString(R.string.hint_not_provider_gps))
            return
        }
        locationService.latitude.observe(viewLifecycleOwner) { myLatitude = it }
        locationService.longitude.observe(viewLifecycleOwner) { myLongitude = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationService.stopListener(mActivity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        // 取得使用者區域設定
        lifecycleScope.launchWhenCreated {
            viewModel.userRegion.collect { region ->
                regionID = regionList.indexOf(region)
                placesSearch()
            }
        }

        // Google Places Search
        lifecycleScope.launchWhenCreated {
            viewModel.placeSearchState.collect {
                when (it) {
                    is Resource.Loading -> {
                        logE("Places Search", "Loading")
                        dialog.showLoadingDialog(false)
                    }
                    is Resource.Success -> {
                        logE("Places Search", "Success")
                        dialog.cancelLoadingDialog()
                    }
                    is Resource.Error -> {
                        logE("Places Search", "Error:${it.message.toString()}")
                        dialog.cancelLoadingDialog()
                        requireActivity().displayShortToast(getString(R.string.hint_error))
                    }
                    else -> Unit
                }
            }
        }

        // Quick View
        initQuickView()
        lifecycleScope.launchWhenCreated {
            viewModel.placeSearch.observe(viewLifecycleOwner) { placesSearch ->
                quickViewAdapter.setData(placesSearch.results as ArrayList<Result>)
            }
        }
    }

    private fun setListener() {
        binding.run {
            tvCategory.setOnClickListener(onClickListener)
            searchBar.setOnClickListener(onClickListener)
            imgCameraSearch.setOnClickListener(onClickListener)
            imgSoundSearch.setOnClickListener(onClickListener)
        }
    }

    private val onClickListener = View.OnClickListener { view: View ->
        val anim = animManager.smallToLarge
        view.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                when (view) {
                    binding.tvCategory -> displayRegionDialog()
                    binding.searchBar -> mActivity.displayShortToast("Search")
                    binding.imgCameraSearch -> mActivity.displayShortToast("Camera")
                    binding.imgSoundSearch -> mActivity.displayShortToast("Sound")
                    else -> {}
                }
            }

            override fun onAnimationEnd(p0: Animation?) {}

            override fun onAnimationRepeat(p0: Animation?) {}
        })
    }

    private fun displayRegionDialog() {
        val dialogBinding = DialogPromptSelectBinding.inflate(layoutInflater)
        val regionSelectAdapter = RegionSelectAdapter()
        dialog.showCenterDialog(true, dialogBinding, false).let {
            dialogBinding.run {
                // initialize
                titleText = getString(R.string.hint_select_region)
                hideCancel = true
                hideConfirm = true
                listItem.apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = regionSelectAdapter
                }
                regionSelectAdapter.setRegionList(regionList, regionID)
                // auto scroll to top
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }
                smoothScroller.targetPosition = regionID
                listItem.layoutManager?.startSmoothScroll(smoothScroller)
                // listener
                regionSelectAdapter.onItemClick = { region ->
                    viewModel.putUserRegion(region)
                    dialog.cancelCenterDialog()
                }
            }
        }
    }

    private fun placesSearch() {
        viewModel.placesSearch(
            regionList[regionID],
            "$myLatitude,$myLongitude",
            mActivity.appInfo().metaData["GOOGLE_KEY"].toString()
        )
    }

    private fun initQuickView() {
        quickViewAdapter = QuickViewAdapter()
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.apply {
            addTransformer(MarginPageTransformer(10))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + (r * 0.15f)
            }
        }
        binding.vpQuickView.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(compositePageTransformer)
            adapter = quickViewAdapter
        }
    }

//    private fun checkTdxToken() {
//        viewModel.getUserTdxTokenUpdate()
//        viewModel.userTdxTokenUpdate.observe(viewLifecycleOwner) { oldDate ->
//            val todayDate: String = SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).format(Date())
//            val tdxTokenReq = TdxTokenReq(
//                mActivity.appInfo().metaData["tdx_grant_type"].toString(),
//                mActivity.appInfo().metaData["tdx_client_id"].toString(),
//                mActivity.appInfo().metaData["tdx_client_secret"].toString()
//            )
//            if (todayDate > oldDate)
//                viewModel.updateTdxToken(todayDate, tdxTokenReq)
//        }
//    }

    companion object {
        private const val DEFAULT_LATITUDE = 25.043871531367014
        private const val DEFAULT_LONGITUDE = 121.53453374432904
    }
}