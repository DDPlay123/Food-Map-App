package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.setAnimClick
import com.side.project.foodmap.ui.adapter.QuickViewAdapter
import com.side.project.foodmap.ui.adapter.RegionSelectAdapter
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.HomeViewModel
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Method.logE
import com.side.project.foodmap.util.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList
import kotlin.math.abs

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: HomeViewModel by viewModel()

    private lateinit var regionList: ArrayList<String>
    private var regionID: Int = 0

    private lateinit var quickViewAdapter: QuickViewAdapter

    init {
        Method.getFcmToken { token -> viewModel.putFcmToken(token) }
    }

    override fun FragmentHomeBinding.initialize() {
        initLocationService()
        binding.vm = viewModel
        regionList = ArrayList(listOf(*resources.getStringArray(R.array.search_type)))
        // Call Data
        viewModel.getAccessKeyFromDataStore()
        viewModel.getUserUIDFromDataStore()
        viewModel.getDeviceId()
        viewModel.getUserRegionFromDataStore()
        viewModel.getUserPictureFromDataStore()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        // 傳送 FCM Token
        lifecycleScope.launchWhenCreated {
            viewModel.putFcmTokenState.collect {
                when (it) {
                    is Resource.Loading -> {
                        logE("FCM Put", "Loading")
                        dialog.showLoadingDialog(false)
                    }
                    is Resource.Success -> {
                        logE("FCM Put", "Success")
                        dialog.cancelLoadingDialog()
                        viewModel.getUserImage()
                    }
                    is Resource.Error -> {
                        logE("FCM Put", "Error:${it.message.toString()}")
                        dialog.cancelLoadingDialog()
                        requireActivity().displayShortToast(getString(R.string.hint_error))
                        viewModel.getUserImage()
                    }
                    else -> Unit
                }
            }
        }

        // 取得使用者照片
        lifecycleScope.launchWhenCreated {
            viewModel.getUserImageState.collect {
                when (it) {
                    is Resource.Loading -> {
                        logE("Get User Image", "Loading")
                        dialog.showLoadingDialog(false)
                    }
                    is Resource.Success -> {
                        logE("Get User Image", "Success")
                        dialog.cancelLoadingDialog()
                        it.data?.result?.let { result ->
                            viewModel.putUserPicture(result.userImage)
                        }
                    }
                    is Resource.Error -> {
                        logE("Get User Image", "Error:${it.message.toString()}")
                        dialog.cancelLoadingDialog()
                        requireActivity().displayShortToast(getString(R.string.hint_error))
                    }
                    else -> Unit
                }
            }
        }

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
                quickViewAdapter.setData(placesSearch.results)
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

            searchBar.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    mActivity.displayShortToast("Search")
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
        }
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
                    dialog.cancelCenterDialog()
                }
            }
        }
    }

    private fun placesSearch() {
//        viewModel.placesSearch(
//            regionList[regionID], "$myLatitude,$myLongitude",
//            mActivity.appInfo().metaData["GOOGLE_KEY"].toString()
//        )
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
}