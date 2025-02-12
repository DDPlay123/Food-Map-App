package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.utsman.geolib.polyline.data.StackAnimationMode
import com.utsman.geolib.polyline.point.PointPolyline
import com.utsman.geolib.polyline.polyline.PolylineAnimator
import com.utsman.geolib.polyline.utils.createPolylineAnimatorBuilder
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.Configs
import mai.project.core.extensions.DP
import mai.project.core.extensions.getColorCompat
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.checkGPSAndGetCurrentLocation
import mai.project.foodmap.base.checkLocationPermission
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.databinding.FragmentRestaurantDetailBinding
import mai.project.foodmap.domain.models.RestaurantRouteResult
import mai.project.foodmap.domain.state.NetworkResult
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class RestaurantDetailFragment : BaseFragment<FragmentRestaurantDetailBinding, RestaurantDetailViewModel>(
    bindingInflater = FragmentRestaurantDetailBinding::inflate
), OnMapReadyCallback, OnMapLoadedCallback, OnCameraIdleListener {
    override val viewModel by viewModels<RestaurantDetailViewModel>()

    private val args by navArgs<RestaurantDetailFragmentArgs>()

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var myMap: GoogleMap

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var polylineAnimator: PolylineAnimator? = null

    private var pointPolyline: PointPolyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher = googleMapUtil.createLocationPermissionLauncher(
            fragment = this,
            onGranted = { },
            onDenied = { checkLocationPermission(googleMapUtil) }
        )
    }

    override fun FragmentRestaurantDetailBinding.initialize(savedInstanceState: Bundle?) {
        googleMapUtil.launchLocationPermission(locationPermissionLauncher)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapHost) as SupportMapFragment
        mapFragment.getMapAsync(this@RestaurantDetailFragment)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutDetail.root)
        with(bottomSheetBehavior) {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun FragmentRestaurantDetailBinding.destroy() {
        if (::myMap.isInitialized) with(myMap) {
            clear()
            setOnMapLoadedCallback(null)
            setOnCameraIdleListener(null)
        }
        polylineAnimator = null
        pointPolyline = null
    }

    override fun FragmentRestaurantDetailBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 當前位置與目標地的路線
            { routeResult.collect(::handleRouteResult) }
        )
    }

    override fun FragmentRestaurantDetailBinding.setListener() {
        imgBack.onClick { navigateUp() }

        imgMyRoute.onClick {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            fetchRoute()
        }

        imgMyLocation.onClick {
            checkGPSAndGetCurrentLocation(
                googleMapUtil = googleMapUtil,
                onSuccess = { lat, lng -> initLocation(lat, lng) },
                onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
            )
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val height = bottomSheet.height.toFloat()
                val offset = height * slideOffset

                viewModel.targetCameraPosition?.let {
                    when (bottomSheetBehavior.state) {
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                            setMapsPaddingFromBottom(offset)
                            googleMapUtil.updateCamera(myMap, it)
                        }

                        else -> Unit
                    }
                }
            }
        })
    }

    /**
     * 設定地圖與底部的偏移
     */
    private fun setMapsPaddingFromBottom(offset: Float) {
        val maxMapsPaddingBottom = 1f
        myMap.setPadding(0, 0, 0, (offset * maxMapsPaddingBottom).roundToInt())
    }

    override fun onMapReady(maps: GoogleMap) {
        myMap = maps.apply {
            googleMapUtil.doInitializeGoogleMap(this)
            googleMapUtil.setCompassLocation(
                mapFragment = mapFragment,
                marginTop = 45.DP,
                marginLeft = 90.DP
            )
            setOnMapLoadedCallback(this@RestaurantDetailFragment)
            setOnCameraIdleListener(this@RestaurantDetailFragment)
        }
    }

    override fun onMapLoaded() {
        checkGPSAndGetCurrentLocation(
            googleMapUtil = googleMapUtil,
            onSuccess = { lat, lng -> initLocation(lat, lng) },
            onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
        )
        fetchRoute()
    }

    override fun onCameraIdle() {
        if (viewModel.targetCameraPosition != myMap.cameraPosition)
            viewModel.targetCameraPosition = myMap.cameraPosition
    }

    /**
     * 初始化位置
     */
    private fun initLocation(
        lat: Double,
        lng: Double
    ) {
        if (::myMap.isInitialized) {
            googleMapUtil.moveCamera(
                map = myMap,
                latLng = LatLng(lat, lng),
                zoomLevel = 15f
            )
            Handler(Looper.getMainLooper()).postDelayed({
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }, 100)
        }
    }

    /**
     * 請求取得當前位置與目標地的路線
     */
    private fun fetchRoute() {
        checkGPSAndGetCurrentLocation(
            googleMapUtil = googleMapUtil,
            onSuccess = { lat, lng -> viewModel.getRouteResult(args.placeId, lat, lng) },
            onFailure = { viewModel.getRouteResult(args.placeId, Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
        )
    }

    /**
     * 處理當前位置與目標地的路線結果
     */
    private fun handleRouteResult(
        event: Event<NetworkResult<RestaurantRouteResult>>
    ) {
        handleBasicResult(
            event = event,
            workOnSuccess = { data ->
                data?.apply {
                    viewModel.routePoints = googleMapUtil.decodePolyline(polyline)

                    myMap.clear()
                    polylineAnimator = null

                    addDestinationMarker()
                    addDestinationPolyline()
                    zoomMyLocationAndRoute()

                    googleMapUtil.addInfoWindowOnPolyline(
                        map = myMap,
                        routes = viewModel.routePoints,
                        title = String.format(
                            Locale.getDefault(),
                            if (distanceMeters < 1000) getString(R.string.format_number_meter) else getString(R.string.format_number_kilometer),
                            (if (distanceMeters < 1000) distanceMeters else distanceMeters / 1000).toFloat()
                        ),
                        content = formatDuration(duration)
                    )
                }
            }
        )
    }

    /**
     * 新增目標點的 Marker
     */
    private fun addDestinationMarker() {
        myMap.clear()
        val marker = MarkerOptions().apply {
            position(LatLng(args.lat.toDouble(), args.lng.toDouble()))
            title(args.name)
            icon(BitmapDescriptorFactory.fromResource(R.drawable.img_location))
        }
        myMap.addMarker(marker)
    }

    /**
     * 新增目標點的 Polyline
     */
    private fun addDestinationPolyline() {
        if (polylineAnimator == null) {
            val animatorBuilder = myMap.createPolylineAnimatorBuilder()
                .withPrimaryPolyline {
                    color(getColorCompat(R.color.primary))
                    width(24f)
                }
                .withStackAnimationMode(StackAnimationMode.BlockStackAnimation)
            polylineAnimator = animatorBuilder.createPolylineAnimator()
        }
        pointPolyline?.remove(viewModel.routePoints)
        pointPolyline = polylineAnimator?.startAnimate(viewModel.routePoints) {
            duration = 1500
        }
    }

    /**
     * 格式化距離時間
     */
    private fun formatDuration(duration: Int): String {
        val day = duration / 86400
        val hour = (duration / 3600) % 24
        val minute = (duration / 60) % 60
        val second = duration % 60

        val timeParts = mutableListOf<String>()

        if (day > 0) timeParts.add(getString(R.string.format_day, day))
        if (hour > 0) timeParts.add(getString(R.string.format_hour, hour))
        if (minute > 0) timeParts.add(getString(R.string.format_minute, minute))
        if (second > 0 && timeParts.isEmpty()) timeParts.add(getString(R.string.format_second, second))

        return timeParts.joinToString(" ")
    }

    /**
     * 縮放當前位置與目標地
     */
    private fun zoomMyLocationAndRoute() {
        val builder = LatLngBounds.Builder()
        viewModel.routePoints.forEach { builder.include(it) }
        val bounds = builder.build()
        val metrics = resources.displayMetrics
        val cu = CameraUpdateFactory.newLatLngBounds(
            bounds,
            (metrics.widthPixels * 2.2).toInt(),
            metrics.heightPixels,
            (metrics.widthPixels * 0.7).toInt()
        )
        myMap.animateCamera(cu)
    }
}