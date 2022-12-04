package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.restaurant.DrawCardRes
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.databinding.DialogSearchBinding
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.helper.setAnimClick
import com.side.project.foodmap.helper.show
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.activity.ListActivity
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.adapter.PopularSearchAdapter
import com.side.project.foodmap.ui.adapter.RegionSelectAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Method.logE
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.math.abs

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModel()

    private lateinit var regionList: ArrayList<String>
    private lateinit var region: String
    private lateinit var placeId: String
    private var regionID: Int = 0

    private var isRecentPopularSearch: Boolean = true

    private lateinit var popularSearchAdapter: PopularSearchAdapter

    init {
        Method.getFcmToken { token -> viewModel.putFcmToken(token) }
    }

    override fun FragmentHomeBinding.initialize() {
        initLocationService()
        binding.vm = viewModel
        binding.isPopularSearch = isRecentPopularSearch
        regionList = ArrayList(listOf(*resources.getStringArray(R.array.search_type)))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 傳送 FCM Token
                launch {
                    viewModel.putFcmTokenState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                logE("FCM Put", "Loading")
                            }
                            is Resource.Success -> {
                                logE("FCM Put", "Success")
                            }
                            is Resource.Error -> {
                                logE("FCM Put", "Error:${it.message.toString()}")
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 取得使用者區域設定
                launch {
                    viewModel.userRegion.collect { region ->
                        dialog.cancelAllDialog()
                        this@HomeFragment.region = region
                        regionID = regionList.indexOf(region)
                        viewModel.nearSearch(region, LatLng(locationService.getLatitude(), locationService.getLongitude()))
                        viewModel.popularSearch(region, LatLng(locationService.getLatitude(), locationService.getLongitude()),
                            if (isRecentPopularSearch) 0 else 1)
                    }
                }
                // 人氣餐廳
                launch {
                    viewModel.popularSearchState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Popular Search", "Loading")
                                dialog.showLoadingDialog(false)
                                binding.vpPopular.hidden()
                                binding.lottieNoData.show()
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Popular Search", "Success")
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Popular Search", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                viewModel.getDrawCardData()
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 人氣餐廳 From Room
                launch {
                    viewModel.getDrawCard.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Popular Search Room", "Loading")
                                dialog.showLoadingDialog(false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Popular Search Room", "Success")
                                dialog.cancelLoadingDialog()
                                binding.vpPopular.show()
                                binding.lottieNoData.hidden()
                                resource.data?.let { data ->
                                    if (data.result.msg.isNullOrEmpty() && data.result.placeList.isNotEmpty())
                                        initPopularCard(data)
                                    else {
                                        binding.vpPopular.hidden()
                                        binding.lottieNoData.show()
                                    }
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Popular Search Room", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                binding.vpPopular.hidden()
                                binding.lottieNoData.show()
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 附近搜尋
                launch {
                    viewModel.nearSearchState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Near Search", "Loading")
                                dialog.showLoadingDialog(false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Near Search", "Success")
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Near Search", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                viewModel.getDistanceSearchData()
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 附近搜尋 From Room
                launch {
                    viewModel.getDistanceSearch.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Near Search Room", "Loading")
                                dialog.showLoadingDialog(false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Near Search Room", "Success")
                                dialog.cancelLoadingDialog()
                                resource.data?.let { data ->
                                    binding.nearSearch = data
                                    placeId = data.result.placeList[0].uid
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Near Search Room", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
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
        val anim = animManager.smallToLarge
        binding.run {
            tvCategory.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    displayRegionDialog()
                }
            }

            imgUserPicture.setOnClickListener {
                (mActivity as MainActivity).switchFragment(R.id.profilesFragment)
            }

            searchBar.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    displaySearchDialog()
                }
            }

            imgCameraSearch.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    mActivity.displayShortToast("Camera")
                }
            }

            imgSoundSearch.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    mActivity.displayShortToast("Sound")
                }
            }

            tvPopular.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    isRecentPopularSearch = !isRecentPopularSearch
                    togglePopularSearch(isRecentPopularSearch)
                }
            }

            imgRefresh.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::region.isInitialized) {
                        viewModel.nearSearch(region, LatLng(locationService.getLatitude(), locationService.getLongitude()))
                        viewModel.popularSearch(region, LatLng(locationService.getLatitude(), locationService.getLongitude()),
                            if (isRecentPopularSearch) 0 else 1)
                    }
                }
            }

            cardAllRestaurant.setOnClickListener { watchDetail(placeId) }

            tvViewMore.setOnClickListener {
                Bundle().also { b ->
                    val latLng: LatLng = Method.getCurrentLatLng(region, LatLng(locationService.getLatitude(), locationService.getLongitude()))
                    b.putString("TITLE", region)
                    b.putDouble("LATITUDE", latLng.latitude)
                    b.putDouble("LONGITUDE", latLng.longitude)
                    mActivity.start(ListActivity::class.java, b)
                }
            }
        }
    }

    private fun togglePopularSearch(isRecentPopularSearch: Boolean) {
        binding.isPopularSearch = isRecentPopularSearch
        if (isRecentPopularSearch)
            viewModel.popularSearch(region, LatLng(locationService.getLatitude(), locationService.getLongitude()), mode = 0)
        else
            viewModel.popularSearch(region, LatLng(locationService.getLatitude(), locationService.getLongitude()), mode = 1)
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
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
                    dialog.showLoadingDialog(false)
                }
            }
        }
    }

    private fun initPopularCard(drawCardRes: DrawCardRes) {
        popularSearchAdapter = PopularSearchAdapter()
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.apply {
            addTransformer(MarginPageTransformer(10))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + (r * 0.15f)
            }
        }
        binding.vpPopular.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(compositePageTransformer)
            adapter = popularSearchAdapter
            if (drawCardRes.result.placeList.size > 0)
                popularSearchAdapter.setterData(drawCardRes.result.placeList)
        }

        popularSearchAdapter.onItemClick = { watchDetail(it) }
    }

    private fun watchDetail(placeId: String) {
        if (placeId.isEmpty()) return
        try {
            logE("Watch Detail", "Success")
            Bundle().also { b ->
                b.putString("PLACE_ID", placeId)
                mActivity.start(DetailActivity::class.java, b)
            }
        } catch (e: Exception) {
            logE("Watch Detail", "Error")
            requireActivity().displayShortToast(getString(R.string.hint_error))
        }
    }

    private fun displaySearchDialog() {
        val dialogBinding = DialogSearchBinding.inflate(layoutInflater)
        dialog.showBottomDialog(dialogBinding, true).let {
            dialogBinding.run {

            }
        }
    }
}