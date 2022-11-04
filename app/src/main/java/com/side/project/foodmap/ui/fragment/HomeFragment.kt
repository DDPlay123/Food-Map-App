package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.ui.adapter.RegionSelectAdapter
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.viewModel.HomeViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: HomeViewModel by viewModel()
    private val animManager: AnimManager by inject()

    private lateinit var regionList: ArrayList<String>
    private var regionID: Int = 0

    override fun FragmentHomeBinding.initialize() {
        binding.vm = viewModel
        regionList = ArrayList(listOf(*resources.getStringArray(R.array.search_type)))
//        checkTdxToken() // 暫時棄用 TDX API
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        viewModel.getUserRegionFromDataStore()
        viewModel.userRegion.observe(viewLifecycleOwner) { region ->
            regionID = regionList.indexOf(region)
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
            override fun onAnimationStart(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                when (view) {
                    binding.searchBar -> {
                        mActivity.displayShortToast("Search")
                    }
                    binding.imgCameraSearch -> {
                        mActivity.displayShortToast("Camera")
                    }
                    binding.imgSoundSearch -> {
                        mActivity.displayShortToast("Sound")
                    }
                    binding.tvCategory -> { displayRegionDialog() }
                    else -> {}
                }
            }

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
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = regionSelectAdapter
                }
                regionSelectAdapter.setRegionList(regionList, regionID)
                // scroll to top
                val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(context) {
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

//    private fun checkTdxToken() {
//        viewModel.getUserTdxTokenUpdate()
//        viewModel.userTdxTokenUpdate.observe(viewLifecycleOwner) { oldDate ->
//            val todayDate: String = SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).format(Date())
//            if (todayDate > oldDate)
//                viewModel.updateTdxToken(todayDate)
//        }
//    }
}