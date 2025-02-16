package mai.project.foodmap.features.home_features.mapTabScreen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import coil.transform.CircleCropTransformation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import com.utsman.geolib.polyline.point.PointPolyline
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import mai.project.core.Configs
import mai.project.core.extensions.DP
import mai.project.core.extensions.getDrawableCompat
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.checkGPSAndGetCurrentLocation
import mai.project.foodmap.base.checkLocationPermission
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.databinding.FragmentMapTabBinding
import mai.project.foodmap.databinding.LayoutRestaurantMarkerBinding
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MapTabFragment : BaseFragment<FragmentMapTabBinding, MapTabViewModel>(
    bindingInflater = FragmentMapTabBinding::inflate
), OnMapReadyCallback, OnMapLoadedCallback {
    override val viewModel by hiltNavGraphViewModels<MapTabViewModel>(R.id.nav_main)

    override val isNavigationVisible: Boolean = true

    override val useActivityOnBackPressed: Boolean = true

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var myMap: GoogleMap

    private var pointPolyline: PointPolyline? = null

    private var markerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher = googleMapUtil.createLocationPermissionLauncher(
            fragment = this,
            onGranted = { },
            onDenied = { checkLocationPermission(googleMapUtil) }
        )
    }

    override fun FragmentMapTabBinding.initialize(savedInstanceState: Bundle?) {
        googleMapUtil.launchLocationPermission(locationPermissionLauncher)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapHost) as SupportMapFragment
        mapFragment.getMapAsync(this@MapTabFragment)
    }

    override fun FragmentMapTabBinding.destroy() {
        markerJob?.cancel()
        markerJob = null
        if (::myMap.isInitialized) with(myMap) {
            clear()
            setOnMapLoadedCallback(null)
        }
        pointPolyline = null
    }

    override fun FragmentMapTabBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 抓取附近地區的餐廳資訊
            { nearbyRestaurant.collect(::handleNearbyRestaurantResult) },
            // 餐廳列表
            {
                combine(restaurantList, myFavoritePlaceIdList, myBlacklistPlaceIdList) { list, favoriteIds, blacklistIds ->
                    list.map { it.copy(isFavorite = it.placeId in favoriteIds) }
                        .filter { it.placeId !in blacklistIds }
                }.collect(::handleRestaurantList)
            }
        )
    }

    override fun FragmentMapTabBinding.setListener() {
        imgMyLocation.onClick {
            checkGPSAndGetCurrentLocation(
                googleMapUtil = googleMapUtil,
                onSuccess = { lat, lng -> initLocation(lat, lng, false) },
                onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE, false) }
            )
        }

        imgRoute.onClick {

        }

        imgSearch.onClick {

        }
    }

    override fun onMapReady(maps: GoogleMap) {
        myMap = maps.apply {
            googleMapUtil.doInitializeGoogleMap(this)
            googleMapUtil.setCompassLocation(
                mapFragment = mapFragment,
                marginTop = 45.DP,
                marginLeft = 45.DP
            )
            setOnMapLoadedCallback(this@MapTabFragment)
        }
    }

    override fun onMapLoaded() {
        checkGPSAndGetCurrentLocation(
            googleMapUtil = googleMapUtil,
            onSuccess = { lat, lng -> initLocation(lat, lng, true) },
            onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE, true) }
        )
    }

    /**
     * 初始化位置
     */
    private fun initLocation(
        lat: Double,
        lng: Double,
        needFetchData: Boolean
    ) {
        if (::myMap.isInitialized) {
            googleMapUtil.animateCamera(
                map = myMap,
                latLng = LatLng(lat, lng),
                zoomLevel = 15f,
                finish = {
                    if (needFetchData) {
                        calculateDistanceAndFetchData(lat = lat, lng = lng)
                    }
                }
            )
        }
    }

    /**
     * 計算當前地圖可顯示距離後，抓取資料
     */
    private fun calculateDistanceAndFetchData(
        lat: Double, lng: Double
    ) {
        val visibleRegion = myMap.projection.visibleRegion
        val distance = SphericalUtil.computeDistanceBetween(
            // 圓半徑
            visibleRegion.nearLeft, myMap.cameraPosition.target
        )
        viewModel.getNearbyRestaurant(lat, lng, distance.roundToInt())
    }

    /**
     * 處理附近餐廳結果
     */
    private fun handleNearbyRestaurantResult(
        event: Event<NetworkResult<List<RestaurantResult>>>
    ) {
        handleBasicResult(event,
            workOnSuccess = { it?.let(viewModel::setRestaurantList) },
            workOnError = { viewModel.setRestaurantList(emptyList()) }
        )
    }

    /**
     * 處理餐廳列表
     */
    private fun handleRestaurantList(list: List<RestaurantResult>) {
        addMarkers(list)
    }

    /**
     * 新增地圖 Marker
     */
    private fun addMarkers(list: List<RestaurantResult>) {
        if (!::myMap.isInitialized) return
        myMap.clear()
        markerJob?.cancel()
        markerJob = viewLifecycleOwner.lifecycleScope.launch {
            list.forEach { item ->
                val markerView = LayoutRestaurantMarkerBinding.inflate(layoutInflater)
                ImageLoaderUtil.asyncLoadDrawable(
                    context = requireContext(),
                    lifecycle = viewLifecycleOwner.lifecycle,
                    imageUrl = item.photos.firstOrNull(),
                    transformation = CircleCropTransformation()
                ) { drawable ->
                    if (drawable != null) {
                        markerView.imgMarker.setImageDrawable(drawable)
                    } else {
                        markerView.imgMarker.setImageDrawable(
                            getDrawableCompat(R.drawable.bg_restaurant_empty)
                        )
                    }
                    val markerBitmap = getBitmapFromView(markerView.root)
                    val markerOptions = MarkerOptions()
                        .position(LatLng(item.lat, item.lng))
                        .title(item.name)
                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                    myMap.addMarker(markerOptions)
                }
            }
        }
    }

    /**
     * 將 view 轉換成 Bitmap，用來建立自定義 Marker
     */
    private fun getBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}