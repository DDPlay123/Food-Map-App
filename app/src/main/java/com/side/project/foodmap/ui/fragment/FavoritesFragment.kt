package com.side.project.foodmap.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.FragmentFavoritesBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.adapter.FavoriteListAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.tools.Method.logE
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.NetworkConnection
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>(R.layout.fragment_favorites) {
    private val viewModel: MainViewModel by activityViewModel()
    private val networkConnection: NetworkConnection by inject()
    private var map: GoogleMap? = null

    // Toole
    private lateinit var favoriteListAdapter: FavoriteListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // Wait pull favorite list
    private lateinit var favoriteList: FavoriteList

    override fun FragmentFavoritesBinding.initialize() {
        initLocationService()
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()
    }

    @SuppressLint("MissingPermission")
    private fun initGoogleMap() {
        map?.apply {
            uiSettings.setAllGesturesEnabled(true)
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                LatLng(myLatitude, myLongitude), DEFAULT_ZOOM
            ))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        doInitialize()
        initLayoutOption()
        setListener()
    }

    private fun doInitialize() {
        networkConnection.observe(viewLifecycleOwner) { isConnect ->
            if (isConnect)
                viewModel.getSyncFavoriteList()
            else
                dialog.cancelAllDialog()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 取的最愛清單
                launch {
                    viewModel.getFavoriteListState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Get Favorite List", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Get Favorite List", "Success")
                                dialog.cancelLoadingDialog()
                                resource.data?.let { data ->
                                    if (data.result.isNotEmpty()) {
                                        binding.layoutOption.rvFavorites.display()
                                        binding.layoutOption.lottieNoData.hidden()
                                    } else {
                                        binding.layoutOption.rvFavorites.hidden()
                                        binding.layoutOption.lottieNoData.display()
                                    }
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Get Favorite List", "Error：${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 取的最愛清單 (已同步的)
                launch {
                    viewModel.currentFavoriteList.observe(viewLifecycleOwner) { favoriteLists ->
                        favoriteLists.let { favoriteListAdapter.setData(it) }

                        if (favoriteLists.isNotEmpty()) {
                            binding.layoutOption.rvFavorites.display()
                            binding.layoutOption.lottieNoData.hidden()
                            setMapMarkers(favoriteLists)
                        } else {
                            binding.layoutOption.rvFavorites.hidden()
                            binding.layoutOption.lottieNoData.display()
                        }
                    }
                }
                // 刪除最愛
                launch {
                    viewModel.pullFavoriteState.collect {
                        when (it) {
                            is Resource.Success -> {
                                logE("Pull Favorite", "Success")
                                if (::favoriteList.isInitialized) {
                                    viewModel.deleteFavoriteData(favoriteList)
                                    viewModel.getSyncFavoriteList()
                                }
                            }
                            is Resource.Error -> {
                                logE("Pull Favorite", "Error:${it.message.toString()}")
                                requireActivity().displayShortToast(getString(R.string.hint_failed_pull_favorite))
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
            layoutOption.imgUpToTop.setOnClickListener {
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }
                smoothScroller.targetPosition = 0
                layoutOption.rvFavorites.layoutManager?.startSmoothScroll(smoothScroller)
            }
        }
    }

    private fun setMapMarkers(favoriteLists: List<FavoriteList>) {
        map?.apply {
            clear()
            favoriteLists.forEach { favoriteList ->
                val markerOption = MarkerOptions().apply {
                    position(LatLng(favoriteList.location.lat, favoriteList.location.lng))
                    title(favoriteList.name)
                }
                addMarker(markerOption)
            }
        }
    }

    private fun initLayoutOption() {
        // https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior?hl=en#setfittocontents
        binding.apply {
            // 初始化
            bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutOption.layoutFavoriteList)
            with (bottomSheetBehavior) {
                isFitToContents = false
                halfExpandedRatio = 0.5f
                expandedOffset = mActivity.getStatusBarHeight()
                state = BottomSheetBehavior.STATE_HALF_EXPANDED

                layoutOption.tvTitle.setOnClickListener {
                    state = if (state == BottomSheetBehavior.STATE_COLLAPSED)
                        BottomSheetBehavior.STATE_HALF_EXPANDED
                    else
                        BottomSheetBehavior.STATE_COLLAPSED
                }
            }

            favoriteListAdapter = FavoriteListAdapter()
            layoutOption.rvFavorites.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = favoriteListAdapter
                setRvItemListener()
            }
        }
    }

    private fun setRvItemListener() {
        if (!::favoriteListAdapter.isInitialized) return
        favoriteListAdapter.apply {
            onItemClick = { favoriteList ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                map?.apply {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(favoriteList.location.lat, favoriteList.location.lng), DEFAULT_ZOOM))
                }
            }

            onItemPullFavorite = { item ->
                favoriteList = item
                displayRemoveFavoriteDialog()
            }

            onItemWebsite = { website ->
                if (website.isNotEmpty()) {
                    Intent(Intent.ACTION_VIEW).also { i ->
                        i.data = Uri.parse(website)
                        startActivity(i)
                    }
                } else
                    requireActivity().displayShortToast(getString(R.string.hint_no_website))
            }

            onItemDetail = { placeId ->
                try {
                    logE("Watch Detail", "Success")
                    Bundle().also { b ->
                        b.putString(Constants.PLACE_ID, placeId)
                        mActivity.start(DetailActivity::class.java, b)
                    }
                } catch (e: Exception) {
                    logE("Watch Detail", "Error")
                    requireActivity().displayShortToast(getString(R.string.hint_error))
                }
            }

            onItemPhone = { phone ->
                if (phone.isNotEmpty()) {
                    Intent(Intent.ACTION_DIAL).also { i ->
                        i.data = Uri.parse("tel:$phone")
                        startActivity(i)
                    }
                } else
                    requireActivity().displayShortToast(getString(R.string.hint_no_phone))
            }

            onItemShare = { url ->
                val share = Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    type="text/plain"
                    putExtra(Intent.EXTRA_TEXT, url)
                    putExtra(Intent.EXTRA_TITLE, getString(R.string.hint_share_url_title))
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }, getString(R.string.hint_share_url_title))
                startActivity(share)
            }
        }
    }

    private fun displayRemoveFavoriteDialog() {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    imgPromptIcon.setImageResource(R.drawable.ic_favorite)
                    titleText = getString(R.string.hint_prompt_remove_favorite_title)
                    tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                    tvConfirm.setOnClickListener {
                        viewModel.pullFavorite(arrayListOf(favoriteList.placeId))
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 18F
    }
}