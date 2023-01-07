package com.side.project.foodmap.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.SetLocation
import com.side.project.foodmap.databinding.FragmentMapsBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.adapter.MapRestaurantAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.animPolyline.AnimatedPolyline
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {
    private val viewModel: MainViewModel by activityViewModel()
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var mLoc: LatLng
    private lateinit var animatedPolyline: AnimatedPolyline

    private lateinit var mapRestaurantAdapter: MapRestaurantAdapter

    private lateinit var oldLatLng: Location

    private var isSlide: Boolean = true

    override fun FragmentMapsBinding.initialize() {
        mActivity.initLocationService()
        viewModel.distanceSearch(
            if (viewModel.isUseMyLocation) Location(
                mActivity.myLatitude,
                mActivity.myLongitude
            ) else viewModel.selectLatLng
        )
    }

    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()
        setTrackMyLocation()

        map.setOnMarkerClickListener { marker ->
            binding?.isTrack = false
            viewModel.isTrack = false
            val mask = marker.snippet.toString().split(",")
            val position = mask[0]
            binding?.vpRestaurant?.currentItem = position.toInt()
            true
        }

        map.setOnCameraMoveListener {
            map.cameraPosition.target.let {
                mLoc = it
            }
        }

        map.setOnCameraMoveStartedListener {
            when (it) {
                REASON_GESTURE -> {
                    binding?.isTrack = false
                    viewModel.isTrack = false
                }
            }
        }
    }

    private fun setTrackMyLocation() {
        if (!mActivity.checkDeviceGPS() && !mActivity.checkNetworkGPS()) {
            mActivity.displayShortToast(getString(R.string.hint_not_provider_gps))
            return
        }
        mActivity.locationGet.observe(this) {
            if (!viewModel.isTrack) return@observe
            setMyLocation()
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
            setCompass()
        }
    }

    private fun setMyLocation() {
        viewModel.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                if (!::map.isInitialized) return@launch
                mLoc = LatLng(mActivity.myLatitude, mActivity.myLongitude)
                binding?.isTrack = true
                viewModel.isTrack = true
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(mActivity.myLatitude, mActivity.myLongitude),
                    DEFAULT_ZOOM
                ))
            }
        }
    }

    private fun setCompass() {
        try {
            mapFragment.view?.let { mapView ->
                mapView.findViewWithTag<View>("GoogleMapMyLocationButton").parent?.let { parent ->
                    val vg: ViewGroup = parent as ViewGroup
                    vg.post {
                        val mapCompass: View = parent.getChildAt(4)
                        val rlp = RelativeLayout.LayoutParams(mapCompass.height, mapCompass.height)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)

                        val topMargin = (40 * Resources.getSystem().displayMetrics.density).toInt()
                        val startMargin = (20 * Resources.getSystem().displayMetrics.density).toInt()
                        rlp.setMargins(startMargin, topMargin, 0, 0)
                        mapCompass.layoutParams = rlp
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog.showLoadingDialog(mActivity, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        doInitialize()
        initRestaurantVp()
        setListener()
    }

    override fun onDestroyView() {
        map.clear()
        viewModel.mapPolylineArray = emptyList()
        super.onDestroyView()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 附近搜尋
                launch {
                    viewModel.distanceSearchFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    viewModel.distanceSearchRes = data
                                    setMapMarkers()
                                    if (::mapRestaurantAdapter.isInitialized)
                                        mapRestaurantAdapter.submitList(data.result.placeList.toMutableList())
                                }
                            }
                            is Resource.Error -> requireActivity().displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
                // 自動更新
                launch {
                    mActivity.locationGet.observe(viewLifecycleOwner) { location ->
                        if (!::oldLatLng.isInitialized) {
                            oldLatLng = location
                            return@observe
                        }
                        if (Method.getDistance(location, oldLatLng) * 1000 > 100) {
                            oldLatLng = location
                            viewModel.run {
                                distanceSearch(
                                    if (isUseMyLocation) Location(
                                        mActivity.myLatitude,
                                        mActivity.myLongitude
                                    ) else selectLatLng
                                )
                            }
                        }
                    }
                }
                // 路線
                launch {
                    viewModel.getRoutePolylineFlow.collect {
                        when (it) {
                            is Resource.Success -> it.data?.result?.let { data ->
                                viewModel.apply {
                                    mapPolylineArray = Method.decodePolyline(data.polyline)
                                    mapPolylineDistance = data.distanceMeters
                                    mapPolylineDuration = data.duration
                                }
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
            }

            imgMyRoute.setOnClickListener {
                binding?.isTrack = false
                viewModel.isTrack = false
                viewModel.apply {
                    getPolyLine(
                        origin = SetLocation(
                            lat = mActivity.myLatitude,
                            lng = mActivity.myLongitude,
                            place_id = ""
                        ),
                        destination = SetLocation(
                            lat = lat,
                            lng = lng,
                            place_id = placeId
                        )
                    )
                }
            }

            imgSearchHere.setOnClickListener {
                if (::animatedPolyline.isInitialized)
                    animatedPolyline.remove()
                isSlide = false
                // 取得中心點到最近的邊之距離(公尺)
                val visibleRegion: VisibleRegion = map.projection.visibleRegion
                val distance = SphericalUtil.computeDistanceBetween(
                    visibleRegion.nearLeft, map.cameraPosition.target
                )
                viewModel.distanceSearch(
                    location = Location(mLoc.latitude, mLoc.longitude),
                    distance = distance.toInt()
                )
            }
        }
    }

    private fun doMapPolyLine() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (::animatedPolyline.isInitialized)
                animatedPolyline.remove()

            map.clear()
            val markerOption = MarkerOptions().apply {
                position(LatLng(viewModel.lat, viewModel.lng))
                title(viewModel.placeName)
                icon(BitmapDescriptorFactory.fromResource(R.drawable.img_location))
            }
            map.addMarker(markerOption)

            animatedPolyline = AnimatedPolyline(
                map = map,
                points = viewModel.mapPolylineArray,
                polylineOptions = PolylineOptions()
                    .width(24f)
                    .color(mActivity.getColorCompat(R.color.google_red))
                    .geodesic(true),
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
            animatedPolyline.addInfoWindow(requireContext(), viewModel.mapPolylineDistance, viewModel.mapPolylineDuration)
            setZoomMap()
        }
    }

    private fun setZoomMap() {
        if (viewModel.mapPolylineArray.size < 2) return
        val builder = LatLngBounds.Builder()
        viewModel.mapPolylineArray.forEach { builder.include(it) }
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

    private fun setMapMarkers() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!::map.isInitialized) return@launch
            map.clear()
                viewModel.distanceSearchRes?.result?.placeList?.forEachIndexed { position, placeList ->
                val markerOption = MarkerOptions().apply {
                    position(LatLng(placeList.location.lat, placeList.location.lng))
                    title(placeList.name)
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.img_location))
                    snippet("${position},")
                }
                map.addMarker(markerOption)
            }
        }
    }

    private fun setCenterLocation(location: Location) {
        if (viewModel.isTrack) return
        map.clear()
        setMapMarkers()
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(location.lat, location.lng),
            DEFAULT_ZOOM
        ))
    }

    private fun initRestaurantVp() {
        mapRestaurantAdapter = MapRestaurantAdapter()
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.apply {
            addTransformer(MarginPageTransformer(20))
        }
        binding?.vpRestaurant?.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(compositePageTransformer)
            adapter = mapRestaurantAdapter
            setVpItemListener()

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (isSlide)
                        setCenterLocation(mapRestaurantAdapter.currentList[position].location)
                    viewModel.apply {
                        index = position
                        placeId = mapRestaurantAdapter.currentList[position].place_id
                        placeName = mapRestaurantAdapter.currentList[position].name
                        lat = mapRestaurantAdapter.currentList[position].location.lat
                        lng = mapRestaurantAdapter.currentList[position].location.lng
                    }
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    isSlide = true
                }
            })
        }
    }

    private fun setVpItemListener() {
        mapRestaurantAdapter.apply {
            onItemClick = { placeList ->
                watchDetail(placeList.place_id)
            }
        }
    }

    private fun watchDetail(placeId: String) {
        if (placeId.isEmpty()) return
        try {
            Method.logE("Watch Detail", "Success")
            Bundle().also { b ->
                b.putString(Constants.PLACE_ID, placeId)
                mActivity.start(DetailActivity::class.java, b)
            }
        } catch (e: Exception) {
            Method.logE("Watch Detail", "Error")
            requireActivity().displayShortToast(getString(R.string.hint_error))
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 18F
    }
}