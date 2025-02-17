package mai.project.foodmap.features.home_features.mapTabScreen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil.transform.CircleCropTransformation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.ClusterManager
import com.utsman.geolib.polyline.data.StackAnimationMode
import com.utsman.geolib.polyline.point.PointPolyline
import com.utsman.geolib.polyline.polyline.PolylineAnimator
import com.utsman.geolib.polyline.utils.createPolylineAnimatorBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mai.project.core.Configs
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.getColorCompat
import mai.project.core.extensions.getDrawableCompat
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.extensions.parcelable
import mai.project.core.extensions.screenWidth
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.core.utils.ImageLoaderUtil
import mai.project.core.widget.recyclerView_decorations.ScaleItemDecoration
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.checkGPSAndGetCurrentLocation
import mai.project.foodmap.base.checkLocationPermission
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateLoadingDialog
import mai.project.foodmap.databinding.FragmentMapTabBinding
import mai.project.foodmap.databinding.LayoutRestaurantMarkerBinding
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.models.RestaurantResult.Companion.ignoredIsFavorite
import mai.project.foodmap.domain.models.RestaurantRouteResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.features.home_features.mapTabScreen.dialog.ClustersCallback
import mai.project.foodmap.features.home_features.mapTabScreen.utils.MyClusterRenderer
import mai.project.foodmap.features.home_features.mapTabScreen.utils.RestaurantClusterItem
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MapTabFragment : BaseFragment<FragmentMapTabBinding, MapTabViewModel>(
    bindingInflater = FragmentMapTabBinding::inflate
), OnMapReadyCallback, OnMapLoadedCallback {
    override val viewModel by viewModels<MapTabViewModel>()

    override val isNavigationVisible: Boolean = true

    override val useActivityOnBackPressed: Boolean = true

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var myMap: GoogleMap

    private lateinit var clusterManager: ClusterManager<RestaurantClusterItem>

    private var polylineAnimator: PolylineAnimator? = null

    private var pointPolyline: PointPolyline? = null

    private var markerJob: Job? = null

    private val mapsRestaurantAdapter by lazy { MapsRestaurantAdapter() }

    private val snapHelper by lazy { LinearSnapHelper() }

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

        with(rvRestaurants) {
            addItemDecoration(
                SpacesItemDecoration(
                    direction = Direction.HORIZONTAL,
                    space = 10.DP
                )
            )
            addItemDecoration(
                ScaleItemDecoration(
                    maxScale = 1f,
                    minScale = .8f
                )
            )
            val offset = requireContext().screenWidth / 2
            setPadding(offset, 0, offset, 0)
            clipToPadding = false
            snapHelper.attachToRecyclerView(this)
            adapter = mapsRestaurantAdapter
        }

        mapFragment = childFragmentManager.findFragmentById(R.id.mapHost) as SupportMapFragment
        mapFragment.getMapAsync(this@MapTabFragment)
    }

    override fun FragmentMapTabBinding.destroy() {
        markerJob?.cancel()
        markerJob = null

        if (::clusterManager.isInitialized) with(clusterManager) {
            clearItems()
            setOnClusterClickListener(null)
        }

        if (::myMap.isInitialized) with(myMap) {
            clear()
            setOnMapLoadedCallback(null)
            setOnCameraIdleListener(null)
        }

        pointPolyline?.remove(viewModel.routePoints)
        polylineAnimator = null
        pointPolyline = null
    }

    override fun FragmentMapTabBinding.setObserver() {
        /* 改為 onMapLoaded 後執行 */
    }

    override fun FragmentMapTabBinding.setListener() {
        imgMyLocation.onClick {
            checkGPSAndGetCurrentLocation(
                googleMapUtil = googleMapUtil,
                onSuccess = { lat, lng -> initLocation(lat, lng) },
                onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
            )
        }

        imgRoute.onClick {
            viewModel.currentItem?.let { item ->
                checkGPSAndGetCurrentLocation(
                    googleMapUtil = googleMapUtil,
                    onSuccess = { lat, lng -> viewModel.getRouteResult(item.placeId, lat, lng) },
                    onFailure = { viewModel.getRouteResult(item.placeId, Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
                )
            }
        }

        imgSearch.onClick {
            if (::myMap.isInitialized) {
                val target = myMap.cameraPosition.target
                calculateDistanceAndFetchData(
                    lat = target.latitude,
                    lng = target.longitude
                )
            }
        }

        mapsRestaurantAdapter.onItemClick = {
            navigate(
                MapTabFragmentDirections.actionMapTabFragmentToRestaurantDetailFragment(
                    placeId = it.placeId,
                    name = it.name,
                    lat = it.lat.toFloat(),
                    lng = it.lat.toFloat()
                )
            )
        }

        mapsRestaurantAdapter.onFavoriteClick = {
            viewModel.pushOrPullMyFavorite(it.placeId, !it.isFavorite)
        }

        rvRestaurants.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                viewModel.currentItem = null
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager ?: return
                    snapHelper.findSnapView(layoutManager)?.let {
                        val position = layoutManager.getPosition(it)
                        mapsRestaurantAdapter.currentList.getOrNull(position)?.let { item ->
                            viewModel.currentItem = item
                            myMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(item.lat, item.lng)))
                        }
                    }
                }
            }
        })
    }

    override fun FragmentMapTabBinding.setCallback() {
        setFragmentResultListener(REQUEST_CODE_CLUSTERS_LIST) { _, bundle ->
            bundle.parcelable<ClustersCallback>(ClustersCallback.ARG_ITEM_CLICK)?.let { callback ->
                callback as ClustersCallback.OnItemClick
                popBackStack(R.id.mapTabFragment, false)
                navigate(
                    MapTabFragmentDirections.actionMapTabFragmentToRestaurantDetailFragment(
                        placeId = callback.item.data.placeId,
                        name = callback.item.data.name,
                        lat = callback.item.data.lat.toFloat(),
                        lng = callback.item.data.lat.toFloat()
                    )
                )
            }
        }
    }

    override fun onMapReady(maps: GoogleMap) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main.immediate) {
            myMap = maps.apply {
                googleMapUtil.doInitializeGoogleMap(this, viewModel.themeMode.first())
                googleMapUtil.setCompassLocation(
                    mapFragment = mapFragment,
                    marginTop = 45.DP,
                    marginLeft = 45.DP
                )
            }

            clusterManager = ClusterManager<RestaurantClusterItem>(requireContext(), myMap)
            clusterManager.renderer = MyClusterRenderer(requireContext(), myMap, clusterManager)
            myMap.setOnCameraIdleListener(clusterManager)
            myMap.setOnMapLoadedCallback(this@MapTabFragment)

            clusterManager.markerCollection.setOnMarkerClickListener { marker ->
                maps.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
                val index = mapsRestaurantAdapter.currentList.indexOfFirst {
                    it.placeId == marker.snippet
                }
                if (index != -1) smoothScrollToPositionWithOffset(index)
                true
            }

            clusterManager.setOnClusterClickListener { cluster ->
                val currentZoom = myMap.cameraPosition.zoom
                val maxZoom = myMap.maxZoomLevel
                if (currentZoom < maxZoom) {
                    // 如果還未縮放至最大，繼續放大
                    val builder = LatLngBounds.builder()
                    cluster.items.forEach { builder.include(it.position) }
                    val bounds = builder.build()
                    myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
                } else {
                    // 已達最大縮放，但叢集仍然存在
                    navigate(
                        MapTabFragmentDirections.actionMapTabFragmentToClustersDialog(
                            requestCode = REQUEST_CODE_CLUSTERS_LIST,
                            clusters = cluster.items.toTypedArray()
                        )
                    )
                }
                true
            }
        }
    }

    override fun onMapLoaded() {
        setObserver()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main.immediate) {
            val myPlaceId = viewModel.myPlaceId.first()
            viewModel.myPlaceList.first().find { it.placeId == myPlaceId }
                ?.let { place -> handleMapLoaded(place.lat, place.lng) }
                ?: run {
                    checkGPSAndGetCurrentLocation(
                        googleMapUtil = googleMapUtil,
                        onSuccess = { lat, lng -> handleMapLoaded(lat, lng) },
                        onFailure = { handleMapLoaded(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
                    )
                }
        }
    }

    /**
     * 設定觀察者
     *
     * - 等待地圖物件初始化完畢
     */
    private fun setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // Loading
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 抓取附近地區的餐廳資訊
            { nearbyRestaurant.collect(::handleNearbyRestaurantResult) },
            // 當前位置與目標地的路線
            { routeResult.collect(::handleRouteResult) },
            // 新增/移除收藏
            { pushOrPullMyFavoriteResult.collect { handleBasicResult(it, false) } },
            // 餐廳列表
            {
                combine(restaurantList, myFavoritePlaceIdList, myBlacklistPlaceIdList) { list, favoriteIds, blacklistIds ->
                    list.map { it.copy(isFavorite = it.placeId in favoriteIds) }
                        .filter { it.placeId !in blacklistIds }
                }.collect(::handleRestaurantList)
            }
        )
    }

    /**
     * 初始化位置
     */
    private fun initLocation(
        lat: Double,
        lng: Double,
        finish: (() -> Unit)? = null
    ) {
        if (::myMap.isInitialized) {
            googleMapUtil.animateCamera(
                map = myMap,
                latLng = LatLng(lat, lng),
                zoomLevel = 18f,
                finish = { finish?.invoke() }
            )
        }
    }

    /**
     * 處理地圖載入完成
     */
    private fun handleMapLoaded(lat: Double, lng: Double) {
        if (!viewModel.isMoveCameraToMyLocation) {
            initLocation(lat, lng) {
                viewModel.isMoveCameraToMyLocation = true
                calculateDistanceAndFetchData(lat, lng)
            }
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
    private fun handleRestaurantList(list: List<RestaurantResult>) = with(binding) {
        // 取得新舊列表
        val oldNormalized = mapsRestaurantAdapter.currentList.map { it.ignoredIsFavorite() }
        val newNormalized = list.map { it.ignoredIsFavorite() }
        // 更新 RecyclerView
        mapsRestaurantAdapter.submitList(list) {
            if (oldNormalized != newNormalized) {
                rvRestaurants.post { smoothScrollToPositionWithOffset(0) }
            }
        }
        // 如果列表不一樣，更新 Marker
        if ((::myMap.isInitialized && ::clusterManager.isInitialized) &&
            (oldNormalized != newNormalized || clusterManager.algorithm.items.isEmpty())
        ) {
            addMarkers(list)
        }
    }

    /**
     * 平滑滾動 RecyclerView 並帶有偏移值
     */
    private fun smoothScrollToPositionWithOffset(
        targetPosition: Int
    ) {
        val layoutManager = binding.rvRestaurants.layoutManager as LinearLayoutManager
        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun calculateDtToFit(
                viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int
            ): Int {
                // 計算偏移值，讓 Item 對齊中心
                val viewCenter = (viewStart + viewEnd) / 2
                val boxCenter = (boxStart + boxEnd) / 2
                return boxCenter - viewCenter
            }
        }
        smoothScroller.targetPosition = targetPosition
        layoutManager.startSmoothScroll(smoothScroller)
    }

    /**
     * 新增地圖 Marker
     */
    private fun addMarkers(list: List<RestaurantResult>) {
        // 清空資源
        myMap.clear()
        clusterManager.clearItems()
        pointPolyline?.remove(viewModel.routePoints)
        viewModel.currentItem = null

        // 設定執行工作
        markerJob?.cancel()
        markerJob = viewLifecycleOwner.lifecycleScope.launch {
            list.forEach { item ->
                val markerView = LayoutRestaurantMarkerBinding.inflate(layoutInflater)
                val photoUrl = item.photos.firstOrNull()
                ImageLoaderUtil.asyncLoadDrawable(
                    context = requireContext(),
                    lifecycle = viewLifecycleOwner.lifecycle,
                    imageUrl = photoUrl,
                    transformation = CircleCropTransformation()
                ) { drawable ->
                    // 設定圖片
                    if (drawable != null) {
                        markerView.imgMarker.setImageDrawable(drawable)
                    } else {
                        markerView.imgMarker.setImageDrawable(
                            getDrawableCompat(R.drawable.bg_restaurant_empty)
                        )
                    }
                    // 轉換為 Bitmap
                    val markerBitmap = getBitmapFromView(markerView.root)
                    // 建立 ClusterItem
                    val clusterItem = RestaurantClusterItem(
                        position = LatLng(item.lat, item.lng),
                        title = item.name,
                        snippet = item.placeId,
                        zIndex = 0f,
                        marker = markerBitmap,
                        data = item
                    )
                    clusterManager.addItem(clusterItem)
                    clusterManager.cluster()
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
                    pointPolyline?.remove(viewModel.routePoints)
                    viewModel.routePoints = googleMapUtil.decodePolyline(polyline)
                    polylineAnimator = null
                    addDestinationPolyline()
                    zoomMyLocationAndRoute()
                }
            }
        )
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
        pointPolyline = polylineAnimator?.startAnimate(viewModel.routePoints) {
            duration = 1500
        }
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

    companion object {
        /**
         * 查看叢集列表
         */
        private const val REQUEST_CODE_CLUSTERS_LIST = "REQUEST_CODE_CLUSTERS_LIST"
    }
}