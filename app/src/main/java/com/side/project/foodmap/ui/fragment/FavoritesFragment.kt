package com.side.project.foodmap.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
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
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.FragmentFavoritesBinding
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.getStatusBarHeight
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.adapter.FavoriteListAdapter
import com.side.project.foodmap.ui.fragment.other.AlbumFragment
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.animPolyline.AnimatedPolyline
import com.side.project.foodmap.util.tools.Method.logE
import com.side.project.foodmap.util.tools.NetworkConnection
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>(R.layout.fragment_favorites) {
    private val viewModel: MainViewModel by activityViewModel()
    private val networkConnection: NetworkConnection by inject()

    // Google Map Tool
    private lateinit var map: GoogleMap
    private lateinit var animatedPolyline: AnimatedPolyline

    // Toole
    private lateinit var favoriteListAdapter: FavoriteListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // Wait pull favorite list
    private lateinit var favoriteList: FavoriteList

    override fun FragmentFavoritesBinding.initialize() {
        mActivity.initLocationService()
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()
    }

    private fun initGoogleMap() {
        if (!requestLocationPermission() || !::map.isInitialized)
            return
        map.apply {
            uiSettings.setAllGesturesEnabled(true)
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                LatLng(mActivity.myLatitude, mActivity.myLongitude), DEFAULT_ZOOM
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
                                    if (data.result.placeList.isNotEmpty()) {
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
        if (!::map.isInitialized) return
        map.apply {
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

    private fun setZoomMap(vararg markers: Marker) {
        if (!::map.isInitialized) return
        val builder = LatLngBounds.Builder()
        markers.forEach { marker ->
            builder.include(marker.position)
        }
        val bounds = builder.build()

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.10).toInt() // offset from edges of the map 10% of screen

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        map.animateCamera(cu)
    }

    private fun setRvItemListener() {
        if (!::favoriteListAdapter.isInitialized) return
        favoriteListAdapter.apply {
            onItemClick = { favoriteList ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                if (::map.isInitialized) {
                    map.apply {
//                    animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        LatLng(favoriteList.location.lat, favoriteList.location.lng), DEFAULT_ZOOM))
                        animatedPolyline = AnimatedPolyline(
                            map= this,
                            points = mutableListOf(
                                LatLng(mActivity.myLatitude, mActivity.myLongitude),
                                LatLng(favoriteList.location.lat, favoriteList.location.lng)),
                            polylineOptions = PolylineOptions()
                                .width(24f)
                                .color(R.color.accent)
                                .pattern(
                                    listOf(
                                        Dot(), Gap(20f)
                                    )
                                ),
                            duration = 1000,
                            interpolator = DecelerateInterpolator(),
                            animatorListenerAdapter = object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    animatedPolyline.start()
                                }
                            }
                        )
                        animatedPolyline.startWithDelay(1000)
                    }
                }
            }

            onPhotoItemClick = { imgView, photos, _, position ->
                Bundle().also {
                    val type = object : TypeToken<List<String>>() {}.type
                    it.putString(Constants.ALBUM_IMAGE_RESOURCE, Gson().toJson(photos, type))
                    it.putInt(Constants.IMAGE_POSITION, position)
//                    val extras = FragmentNavigatorExtras(imgView to imgView.transitionName)
//                    findNavController().navigate(
//                        R.id.action_favoritesFragment_to_albumFragment,
//                        it, null, extras
//                    )
                    val ft = mActivity.supportFragmentManager.beginTransaction()
                    val albumDialog = AlbumFragment()
                    albumDialog.arguments = it
                    val prevDialog = mActivity.supportFragmentManager.findFragmentByTag(Constants.DIALOG_ALBUM)
                    if (prevDialog != null) ft.remove(prevDialog)
                    albumDialog.show(ft, Constants.DIALOG_ALBUM)

                    (mActivity as MainActivity).isHiddenNavigationBar(true)
                    albumDialog.onDismissListener = {
                        (mActivity as MainActivity).isHiddenNavigationBar(false)
                    }
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
                        viewModel.pullFavorite(arrayListOf(favoriteList.place_id))
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