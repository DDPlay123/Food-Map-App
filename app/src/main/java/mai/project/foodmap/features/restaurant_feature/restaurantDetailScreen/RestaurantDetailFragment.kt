package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
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
import mai.project.core.annotations.Direction
import mai.project.core.annotations.NavigationMode
import mai.project.core.extensions.DP
import mai.project.core.extensions.getColorCompat
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.extensions.openGoogleNavigation
import mai.project.core.extensions.openPhoneCall
import mai.project.core.extensions.openUrl
import mai.project.core.extensions.openUrlWithBrowser
import mai.project.core.extensions.parcelable
import mai.project.core.extensions.shareLink
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.core.widget.recyclerView_adapters.ImagePreviewPagerAdapter
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.checkGPSAndGetCurrentLocation
import mai.project.foodmap.base.checkLocationPermission
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateSelectorDialog
import mai.project.foodmap.databinding.FragmentRestaurantDetailBinding
import mai.project.foodmap.domain.models.RestaurantDetailResult
import mai.project.foodmap.domain.models.RestaurantRouteResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.features.dialogs_features.selector.SelectorCallback
import mai.project.foodmap.features.dialogs_features.selector.SelectorModel
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class RestaurantDetailFragment : BaseFragment<FragmentRestaurantDetailBinding, RestaurantDetailViewModel>(
    bindingInflater = FragmentRestaurantDetailBinding::inflate
), OnMapReadyCallback, OnMapLoadedCallback, OnCameraMoveListener {
    override val viewModel by viewModels<RestaurantDetailViewModel>()

    private val args by navArgs<RestaurantDetailFragmentArgs>()

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var myMap: GoogleMap

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val pendingActions = mutableListOf<() -> Unit>()

    private var polylineAnimator: PolylineAnimator? = null

    private var pointPolyline: PointPolyline? = null

    private val photosAdapter by lazy { PhotosAdapter() }

    private val imagePreviewPagerAdapter by lazy { ImagePreviewPagerAdapter() }
    
    private val googleReviewAdapter by lazy { GoogleReviewAdapter() }

    private val navigationModeItems: List<SelectorModel> by lazy {
        resources.getStringArray(R.array.navigation_mode).mapIndexed { index, s ->
            SelectorModel(id = index, content = s)
        }
    }

    private val photoPreviewCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d(message = "圖片預覽滾動位置：$position")
                if (position >= 0 && binding.vpPhotoPreview.isVisible) {
                    binding.layoutDetail.vpPhotos.setCurrentItem(position, false)
                }
            }
        }
    }

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

        with(layoutDetail.vpPhotos) {
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            adapter = photosAdapter
            layoutDetail.piPhotos.attachToViewPager2(this)
        }

        with(vpPhotoPreview) {
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            adapter = imagePreviewPagerAdapter
        }
        
        with(layoutDetail.rvReviews) {
            addItemDecoration(
                SpacesItemDecoration(
                    direction = Direction.VERTICAL,
                    space = 20.DP
                )
            )
            adapter = googleReviewAdapter
        }

        viewModel.getRestaurantDetail(args.placeId)
    }

    override fun FragmentRestaurantDetailBinding.destroy() {
        pendingActions.clear()
        if (::myMap.isInitialized) with(myMap) {
            clear()
            setOnMapLoadedCallback(null)
            setOnCameraMoveListener(null)
        }
        polylineAnimator = null
        pointPolyline = null
        vpPhotoPreview.unregisterOnPageChangeCallback(photoPreviewCallback)
    }

    override fun FragmentRestaurantDetailBinding.handleOnBackPressed() {
        if (binding.vpPhotoPreview.isVisible) {
            closePhotoPreview()
        } else {
            popBackStack()
        }
    }

    override fun FragmentRestaurantDetailBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 當前位置與目標地的路線
            { routeResult.collect(::handleRouteResult) },
            // 餐廳詳細資訊
            { restaurantDetail.collect(::handleDetailResult) },
            // 收藏狀態
            { isFavorite.collect(::setupFavoriteState) },
            // 黑名單狀態
            { isBlocked.collect(::setupBlockedState) },
            // 新增/移除收藏
            { pushOrPullMyFavoriteResult.collect { handleBasicResult(it, false) } },
            // 新增/移除黑名單
            { pushOrPullMyBlackListResult.collect { handleBasicResult(it, false) } }
        )
    }

    override fun FragmentRestaurantDetailBinding.setListener() {
        imgBack.onClick { onBackPressed() }

        imgFavorite.onClick {
            if (viewModel.restaurantDetail.value.getPeekContent.data != null) {
                viewModel.pushOrPullMyFavorite(args.placeId, !viewModel.isFavorite.value)
            }
        }

        imgMyRoute.onClick { fetchRoute() }

        imgMyLocation.onClick {
            checkGPSAndGetCurrentLocation(
                googleMapUtil = googleMapUtil,
                onSuccess = { lat, lng -> initLocation(lat, lng) },
                onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
            )
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        pendingActions.forEach { it.invoke() }
                        pendingActions.clear()
                    }

                    else -> Unit
                }
            }

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

        photosAdapter.onItemClick = { item -> openPhotoPreview(item) }

        imagePreviewPagerAdapter.onClosed = { closePhotoPreview() }

        googleReviewAdapter.onAvatarClick = { requireActivity().openUrl(it) }

        layoutDetail.tvAddress.onClick {
            viewModel.restaurantDetail.value.getPeekContent.data?.let {
                navigateSelectorDialog(
                    requestCode = REQUEST_CODE_NAVIGATION_MODE,
                    items = navigationModeItems
                )
            }
        }

        layoutDetail.tvWorkday.onClick(safe = false) {
            if (viewModel.restaurantDetail.value.getPeekContent.data?.workDay?.isNotEmpty() == true) {
                val isVisible = layoutDetail.tvWorkdayList.isVisible
                layoutDetail.tvWorkdayList.isVisible = !isVisible

                layoutDetail.tvWorkday.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.vector_access_time, 0,
                    if (isVisible) R.drawable.vector_keyboard_arrow_up else R.drawable.vector_keyboard_arrow_down, 0
                )
            }
        }

        layoutDetail.tvWebsite.onClick {
            viewModel.restaurantDetail.value.getPeekContent.data?.website?.let {
                requireActivity().openUrlWithBrowser(it)
            }
        }

        layoutDetail.tvPhone.onClick {
            viewModel.restaurantDetail.value.getPeekContent.data?.phone?.let {
                requireActivity().openPhoneCall(it)
            }
        }

        layoutDetail.tvShare.onClick {
            viewModel.restaurantDetail.value.getPeekContent.data?.shareLink?.let {
                requireActivity().shareLink(getString(R.string.sentence_share_restaurant), it)
            }
        }

        layoutDetail.tvGoogleReview.onClick {
            viewModel.restaurantDetail.value.getPeekContent.data?.shareLink?.let {
                requireActivity().openUrl(it)
            }
        }

        layoutDetail.btnBlocked.onClick {
            if (viewModel.restaurantDetail.value.getPeekContent.data != null) {
                viewModel.pushOrPullMyBlackList(args.placeId, !viewModel.isBlocked.value)
            }
        }
    }

    override fun FragmentRestaurantDetailBinding.setCallback() {
        setFragmentResultListener(REQUEST_CODE_NAVIGATION_MODE) { _, bundle ->
            bundle.parcelable<SelectorCallback>(SelectorCallback.ARG_ITEM_CLICK)?.let { callback ->
                callback as SelectorCallback.OnItemClick
                val latLng = LatLng(
                    viewModel.restaurantDetail.value.getPeekContent.data?.lat ?: Configs.DEFAULT_LATITUDE,
                    viewModel.restaurantDetail.value.getPeekContent.data?.lng ?: Configs.DEFAULT_LONGITUDE
                )
                when (callback.item) {
                    navigationModeItems[0] -> requireActivity().openGoogleNavigation(NavigationMode.CAR, latLng)
                    navigationModeItems[1] -> requireActivity().openGoogleNavigation(NavigationMode.BICYCLE, latLng)
                    navigationModeItems[2] -> requireActivity().openGoogleNavigation(NavigationMode.MOTORCYCLE, latLng)
                    navigationModeItems[3] -> requireActivity().openGoogleNavigation(NavigationMode.WALKING, latLng)
                }
            }
        }
    }

    /**
     * 設定地圖與底部的偏移
     */
    private fun setMapsPaddingFromBottom(offset: Float) {
        val maxMapsPaddingBottom = 1f
        myMap.setPadding(0, 0, 0, (offset * maxMapsPaddingBottom).roundToInt())
    }

    /**
     * 先檢查當前狀態是否為收合，如是收合，則直接執行。否則先暫存並設定 收合狀態
     */
    private fun checkCollapsedStateBeforeDoSomething(
        work: () -> Unit
    ) {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            pendingActions.add(work)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            work.invoke()
        }
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
            setOnCameraMoveListener(this@RestaurantDetailFragment)
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

    override fun onCameraMove() {
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
        checkCollapsedStateBeforeDoSomething {
            if (::myMap.isInitialized) {
                googleMapUtil.animateCamera(
                    map = myMap,
                    latLng = LatLng(lat, lng),
                    zoomLevel = 15f
                )
            }
        }
    }

    /**
     * 請求取得當前位置與目標地的路線
     */
    private fun fetchRoute() {
        checkCollapsedStateBeforeDoSomething {
            checkGPSAndGetCurrentLocation(
                googleMapUtil = googleMapUtil,
                onSuccess = { lat, lng -> viewModel.getRouteResult(args.placeId, lat, lng) },
                onFailure = { viewModel.getRouteResult(args.placeId, Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
            )
        }
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
                    checkCollapsedStateBeforeDoSomething {
                        zoomMyLocationAndRoute()
                    }

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

    /**
     * 處理餐廳詳細資訊結果
     */
    private fun handleDetailResult(
        event: Event<NetworkResult<RestaurantDetailResult>>
    ) {
        handleBasicResult(
            event = event,
            workOnSuccess = { data ->
                viewModel.setIsFavorite(data?.isFavorite ?: false)
                viewModel.setIsBlocked(data?.isBlackList ?: false)
                data?.let(::setupDetailUI)
            }
        )
    }

    /**
     * 設定餐廳詳細資訊 UI
     */
    private fun setupDetailUI(
        data: RestaurantDetailResult
    ) = with(binding.layoutDetail) {
        setupFavoriteState(data.isFavorite)

        pbCircular.isVisible = false
        piPhotos.isVisible = data.photos.isNotEmpty()
        photosAdapter.submitList(data.photos)

        imagePreviewPagerAdapter.submitList(data.photos)

        tvName.text = data.name
        tvRating.text = "${data.ratingStar}"
        rating.rating = data.ratingStar
        tvRatingTotal.text = String.format(Locale.getDefault(), "(%d)", data.ratingTotal)

        tvOpenNow.isVisible = data.openNow != null
        tvOpenNow.text = if (data.openNow == true) getString(R.string.sentence_open_now) else getString(R.string.sentence_close_now)
        tvOpenNow.setTextColor(if (data.openNow == true) getColorCompat(R.color.success) else getColorCompat(R.color.error))

        tvDineIn.isVisible = data.dineIn != null
        tvDineIn.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (data.dineIn == true) R.drawable.vector_check else R.drawable.vector_close,
            0, 0, 0
        )

        tvTakeout.isVisible = data.takeout != null
        tvTakeout.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (data.dineIn == true) R.drawable.vector_check else R.drawable.vector_close,
            0, 0, 0
        )

        tvDelivery.isVisible = data.delivery != null
        tvDelivery.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (data.dineIn == true) R.drawable.vector_check else R.drawable.vector_close,
            0, 0, 0
        )

        tvAddress.text = data.vicinity

        tvWorkday.isVisible = data.workDay.isNotEmpty()
        tvWorkdayList.text = data.workDay.joinToString(separator = "\n") { it }

        tvWebsite.isVisible = !data.website.isNullOrEmpty()
        tvWebsite.text = data.website

        tvPhone.isVisible = !data.phone.isNullOrEmpty()
        tvPhone.text = data.phone

        googleReviewAdapter.submitList(data.reviews)

        btnBlocked.isVisible = true
        setupBlockedState(data.isBlackList)
    }

    /**
     * 設定收藏狀態
     */
    private fun setupFavoriteState(
        isFavorite: Boolean
    ) = with(binding.imgFavorite) {
        if (isFavorite) {
            setImageResource(R.drawable.vector_favorite)
        } else {
            setImageResource(R.drawable.vector_favorite_border)
        }
    }

    /**
     * 設定是否為黑名單
     */
    private fun setupBlockedState(
        isBlackList: Boolean
    ) = with(binding.layoutDetail.btnBlocked) {
        text = if (isBlackList) {
            setBackgroundColor(getColorCompat(R.color.gray))
            getString(R.string.sentence_remove_blacklist)
        } else {
            setBackgroundColor(getColorCompat(R.color.error))
            getString(R.string.sentence_add_blacklist)
        }
    }

    /**
     * 顯示圖片預覽
     */
    private fun openPhotoPreview(item: String) = with(binding.vpPhotoPreview) {
        imagePreviewPagerAdapter.resetState()
        val index = imagePreviewPagerAdapter.currentList.indexOfFirst { it == item }.takeIf { it >= 0 } ?: 0
        Timber.d(message = "設定圖片預覽滾動位置：$index")
        registerOnPageChangeCallback(photoPreviewCallback)
        setCurrentItem(index, false)
        isVisible = true
    }

    /**
     * 關閉圖片預覽
     */
    private fun closePhotoPreview() = with(binding.vpPhotoPreview) {
        unregisterOnPageChangeCallback(photoPreviewCallback)
        isVisible = false
    }

    companion object {
        /**
         * 點擊導航模式 Dialog
         */
        private const val REQUEST_CODE_NAVIGATION_MODE = "REQUEST_CODE_NAVIGATION_MODE"
    }
}