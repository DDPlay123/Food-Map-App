package com.side.project.foodmap.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
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
import com.side.project.foodmap.data.remote.FavoriteList
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.SetLocation
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.FragmentFavoritesBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.adapter.FavoriteListAdapter
import com.side.project.foodmap.ui.fragment.other.AlbumFragment
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.animPolyline.AnimatedPolyline
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.tools.Method.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.*
import kotlin.math.roundToInt

class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>(R.layout.fragment_favorites) {
    private val viewModel: MainViewModel by activityViewModel()

    // Google Map Tool
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var mLoc: LatLng
    private lateinit var animatedPolyline: AnimatedPolyline

    // Tools
    private lateinit var timer: Timer
    private lateinit var favoriteListAdapter: FavoriteListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // Wait pull favorite list
    private lateinit var favoriteList: FavoriteList

    override fun FragmentFavoritesBinding.initialize() {
        mActivity.initLocationService()
        binding?.paddingTop = mActivity.getStatusBarHeight()
        binding?.layoutOption?.paddingTop = mActivity.getStatusBarHeight()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()

        map.setOnCameraMoveListener {
            map.cameraPosition.target.let {
                mLoc = it
            }
        }

        map.setOnMarkerClickListener { marker ->
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding?.layoutOption?.rvFavorites?.scrollToPosition(marker.snippet?.toInt() ?: 0)
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun initGoogleMap() {
        if (!mActivity.requestLocationPermission() || !::map.isInitialized)
            return
        setMyLocation()
        map.apply {
            uiSettings.setAllGesturesEnabled(true)
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog.showLoadingDialog(mActivity, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        viewModel.getSyncFavoriteList()
        doInitialize()
        initLayoutOption()
        setListener()
    }

    override fun onDestroyView() {
        if (::timer.isInitialized)
            timer.cancel()
        map.clear()
        viewModel.favoritePolylineArray = emptyList()
        super.onDestroyView()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 取的最愛清單
                launch {
                    viewModel.syncFavoriteListFlow.collect { favoriteLists ->
                        dialog.cancelLoadingDialog()
                        favoriteLists.let { favoriteListAdapter.setPlaceList(it.toMutableList()) }

                        if (favoriteLists.isNotEmpty()) {
                            binding?.layoutOption?.rvFavorites?.display()
                            binding?.layoutOption?.lottieNoData?.hidden()
                            setMapMarkers(favoriteLists)
                            binding?.layoutOption?.edSearch?.text.toString().trim().let {
                                if (it.isNotEmpty())
                                    filter(it)
                            }
                        } else {
                            binding?.layoutOption?.rvFavorites?.hidden()
                            binding?.layoutOption?.lottieNoData?.display()
                        }
                    }
                }
                // 刪除最愛
                launch {
                    viewModel.pullFavoriteFlow.collect {
                        when (it) {
                            is Resource.Success -> {
                                if (::favoriteList.isInitialized) {
                                    viewModel.deleteFavoriteData(favoriteList)
                                    viewModel.getSyncFavoriteList()
                                }
                            }
                            is Resource.Error -> {
                                requireActivity().displayShortToast(getString(R.string.hint_failed_pull_favorite))
                            }
                            else -> Unit
                        }
                    }
                }
                // 取得路線
                launch {
                    viewModel.getRoutePolylineFlow.collect {
                        when (it) {
                            is Resource.Success -> it.data?.result?.let { data ->
                                viewModel.favoritePolylineArray = Method.decodePolyline(data.polyline)
                                doMapPolyLine()
                            }
                            is Resource.Error -> mActivity.displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun setListener() {
        binding?.run {
            imgMyLocation.setOnClickListener {
                setMyLocation()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            imgMyRoute.setOnClickListener {
                setZoomMap()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            layoutOption.imgUpToTop.setOnClickListener {
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }
                smoothScroller.targetPosition = 0
                layoutOption.rvFavorites.layoutManager?.startSmoothScroll(smoothScroller)
            }

            layoutOption.edSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (::timer.isInitialized)
                        timer.cancel()
                }

                override fun afterTextChanged(editable: Editable?) {
                    val text = editable.toString().trim()
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            Coroutines.main {
                                filter(text)
                            }
                        }
                    }, 500)
                }
            })
        }
    }

    private fun filter(text: String) {
        if (::favoriteListAdapter.isInitialized)
            favoriteListAdapter.filter.filter(text)
    }

    private fun setMyLocation() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!::map.isInitialized) return@launch
            mLoc = LatLng(mActivity.myLatitude, mActivity.myLongitude)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(mActivity.myLatitude, mActivity.myLongitude),
                    DEFAULT_ZOOM
                )
            )
        }
    }

    private fun setMapMarkers(favoriteLists: List<FavoriteList>) {
        if (!::map.isInitialized) return
        map.apply {
            clear()
            favoriteLists.forEachIndexed { index, favoriteList ->
                val markerOption = MarkerOptions().apply {
                    position(LatLng(favoriteList.location.lat, favoriteList.location.lng))
                    title(favoriteList.name)
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.img_location))
                    snippet(index.toString()) // position
                }
                addMarker(markerOption)
            }
        }
    }

    private fun setMapMarkers(targetInfo: FavoriteList, position: Int) {
        if (!::map.isInitialized) return
        map.apply {
            clear()
            val markerOption = MarkerOptions().apply {
                position(LatLng(targetInfo.location.lat, targetInfo.location.lng))
                title(targetInfo.name)
                icon(BitmapDescriptorFactory.fromResource(R.drawable.img_location))
                snippet(position.toString()) // position
            }
            addMarker(markerOption)
        }
    }

    private fun initLayoutOption() {
        // https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior?hl=en#setfittocontents
        binding?.apply {
            // 初始化
            bottomSheetBehavior =
                BottomSheetBehavior.from(binding?.layoutOption?.layoutFavoriteList ?: return)
            with(bottomSheetBehavior) {
                skipCollapsed = false
                isFitToContents = false
                halfExpandedRatio = 0.5F

                state = BottomSheetBehavior.STATE_EXPANDED

                layoutOption.tvTitle.setOnClickListener {
                    state = if (state == BottomSheetBehavior.STATE_COLLAPSED)
                        BottomSheetBehavior.STATE_HALF_EXPANDED
                    else
                        BottomSheetBehavior.STATE_COLLAPSED
                }

                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {

                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        val h = bottomSheet.height.toFloat()
                        val off = h * slideOffset

                        mLoc.let {
                            when (state) {
                                BottomSheetBehavior.STATE_DRAGGING -> {
                                    setMapPaddingBottom(off)
                                    //reposition marker at the center
                                    map.moveCamera(CameraUpdateFactory.newLatLng(mLoc))
                                }
                                BottomSheetBehavior.STATE_SETTLING -> {
                                    setMapPaddingBottom(off)
                                    //reposition marker at the center
                                    map.moveCamera(CameraUpdateFactory.newLatLng(mLoc))
                                }
                                else -> Unit
                            }
                        }
                    }
                })
            }

            favoriteListAdapter = FavoriteListAdapter()
            layoutOption.rvFavorites.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = favoriteListAdapter
                setRvItemListener()
            }
        }
    }

    private fun setMapPaddingBottom(offset: Float) {
        //From 0.0 (min) - 1.0 (max) // bsExpanded - bsCollapsed;
        val maxMapPaddingBottom = 1.0f
        map.setPadding(0, 0, 0, (offset * maxMapPaddingBottom).roundToInt())
    }

    private fun doMapPolyLine() {
        lifecycleScope.launch(Dispatchers.Main) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            if (::animatedPolyline.isInitialized)
                animatedPolyline.remove()

            animatedPolyline = AnimatedPolyline(
                map = map,
                points = viewModel.favoritePolylineArray,
                polylineOptions = PolylineOptions()
                    .width(24f)
                    .color(mActivity.getColorCompat(R.color.google_red))
                    .geodesic(true)
                    .pattern(
                        listOf(
                            Dot(), Gap(20f)
                        )
                    ),
                duration = 1500,
                interpolator = DecelerateInterpolator(),
                animatorListenerAdapter = object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        animatedPolyline.start()
                    }
                }
            )
            animatedPolyline.start()
            setZoomMap()
        }
    }

    private fun setZoomMap() {
        if (viewModel.favoritePolylineArray.size < 2) return
        val builder = LatLngBounds.Builder()
        viewModel.favoritePolylineArray.forEach { builder.include(it) }
        val bounds = builder.build()
        val metrics = resources.displayMetrics
        val cu = CameraUpdateFactory.newLatLngBounds(
            bounds,
            (metrics.widthPixels * 2.2).toInt(),
            metrics.heightPixels,
            (metrics.widthPixels * 0.7).toInt()
        )
        map.moveCamera(cu)
    }

    private fun setRvItemListener() {
        if (!::favoriteListAdapter.isInitialized) return
        favoriteListAdapter.apply {
            onItemClick = { favoriteList, position ->
                Coroutines.main {
                    if (mActivity.checkMyDeviceGPS()) {
                        binding?.layoutOption?.rvFavorites?.scrollToPosition(position)
                        delay(100)
                        setMapMarkers(favoriteList, position)
                        viewModel.getPolyLine(
                            origin = SetLocation(
                                lat = mActivity.myLatitude,
                                lng = mActivity.myLongitude,
                                place_id = ""
                            ),
                            destination = SetLocation(
                                favoriteList.location.lat,
                                favoriteList.location.lng,
                                favoriteList.place_id
                            )
                        )
                    }
                }
            }

            onPhotoItemClick = { photos, position ->
                Bundle().also {
                    val type = object : TypeToken<List<String>>() {}.type
                    it.putString(Constants.ALBUM_IMAGE_RESOURCE, Gson().toJson(photos, type))
                    it.putInt(Constants.IMAGE_POSITION, position)

                    val ft = mActivity.supportFragmentManager.beginTransaction()
                    val albumDialog = AlbumFragment()
                    albumDialog.arguments = it

                    val prevDialog =
                        mActivity.supportFragmentManager.findFragmentByTag(Constants.DIALOG_ALBUM)
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
                    type = "text/plain"
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