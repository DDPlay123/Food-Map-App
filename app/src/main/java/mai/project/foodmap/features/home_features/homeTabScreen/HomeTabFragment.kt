package mai.project.foodmap.features.home_features.homeTabScreen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.checkMultiplePermissions
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.hasGPS
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.extensions.openAppSettings
import mai.project.core.extensions.openGpsSettings
import mai.project.core.extensions.parcelable
import mai.project.core.extensions.requestMultiplePermissions
import mai.project.core.extensions.screenWidth
import mai.project.core.utils.Event
import mai.project.core.utils.ImageLoaderUtil
import mai.project.core.widget.recyclerView_decorations.ScaleItemDecoration
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.data.annotations.DrawCardMode
import mai.project.foodmap.databinding.FragmentHomeTabBinding
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.utils.handleResult
import mai.project.foodmap.features.myPlace_feature.myPlaceDialog.MyPlaceCallback
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding, HomeTabViewModel>(
    bindingInflater = FragmentHomeTabBinding::inflate
) {
    override val viewModel by hiltNavGraphViewModels<HomeTabViewModel>(R.id.nav_main)

    override val useActivityOnBackPressed: Boolean = true

    @Inject
    lateinit var imageLoaderUtil: ImageLoaderUtil

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val drawCardAdapter by lazy { DrawCardAdapter(imageLoaderUtil) }

    private val snapHelper by lazy { LinearSnapHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher = requestMultiplePermissions(
            allPermissions = locationPermissions,
            needAllPermissions = false,
            onGranted = ::handleLocationPermissionGranted,
            onDenied = ::handleLocationPermissionDenied
        )
    }

    override fun FragmentHomeTabBinding.initialize(savedInstanceState: Bundle?) {
        locationPermissionLauncher.launch(locationPermissions)

        with(rvPopular) {
            addItemDecoration(
                SpacesItemDecoration(
                    direction = Direction.HORIZONTAL,
                    space = 20.DP
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
            adapter = drawCardAdapter
        }
    }

    override fun FragmentHomeTabBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // Loading
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 人氣餐廳卡片模式
            { drawCardMode.collect(::handleDrawCardMode) },
            // 當前定位點資訊
            { myPlaceList.combine(myPlaceId) { p0, p1 -> p0 to p1 }.collect { handleMyPlace(it.first, it.second) } },
            // 抓取儲存的定位點資訊
            { myPlaceListResult.collect(::handleMyPlaceListResult) },
            // 抽取人氣餐廳卡片資訊
            { drawCardResult.collect(::handleDrawCardResult) },
            // 人氣餐廳資料列表
            { drawCardList.collect(::handleDrawCardList) }
        )
    }

    override fun FragmentHomeTabBinding.setListener() {
        tvLocation.onClick(anim = true) {
            if (checkLocationPermissionAndGPS()) {
                navigate(
                    HomeTabFragmentDirections.actionHomeTabFragmentToMyPlaceBottomSheetDialog(
                        requestCode = REQUEST_CODE_SELECT_PLACE
                    )
                )
            }
        }

        clTextSearch.onClick {
            // TODO 文字搜尋
        }

        imgVoiceSearch.onClick {
            // TODO 語音搜尋
        }

        tvPopular.onClick(anim = true) { viewModel.setDrawCardMode() }

        imgRefresh.onClick(anim = true) { if (checkLocationPermissionAndGPS()) viewModel.fetchMyPlaceList() }

        cardMore.onClick {
            // TODO 餐廳列表
        }
    }

    override fun FragmentHomeTabBinding.setCallback() {
        setFragmentResultListener(REQUEST_CODE_SELECT_PLACE) { _, bundle ->
            bundle.parcelable<MyPlaceCallback>(MyPlaceCallback.ARG_ITEM_CLICK)?.let tag@ { callback ->
                callback as MyPlaceCallback.OnItemClick
                if (viewModel.myPlaceId.value == callback.placeId) return@tag
                viewModel.setMyPlaceId(callback.placeId)
                viewModel.myPlaceList.value.find { it.placeId == callback.placeId }
                    ?.let { place -> updateLocationAndDrawCard(place.lat, place.lng) }
                    ?: getCurrentLocation(
                        onSuccess = ::updateLocationAndDrawCard,
                        onFailure = { displayToast(getString(R.string.sentence_can_not_get_location)) }
                    )
            }
            bundle.parcelable<MyPlaceCallback>(MyPlaceCallback.ARG_ADD_ADDRESS)?.let {
                // TODO 新增定位點
            }
        }
    }

    /**
     * 檢查定位權限 和 GPS 是否開啟
     */
    private fun checkLocationPermissionAndGPS(): Boolean {
        return when {
            !checkMultiplePermissions(locationPermissions, false) -> {
                handleLocationPermissionDenied()
                false
            }

            !requireContext().hasGPS -> {
                with((activity as? MainActivity)) {
                    this?.showSnackBar(
                        message = getString(R.string.sentence_gps_not_open),
                        actionText = getString(R.string.word_confirm)
                    ) { openGpsSettings() }
                }
                false
            }

            else -> true
        }
    }

    /**
     * 處理定位權限請求成功結果
     */
    private fun handleLocationPermissionGranted() {
        if (checkLocationPermissionAndGPS() && viewModel.drawCardList.value.isEmpty()) {
            viewModel.fetchMyPlaceList()
        }
    }

    /**
     * 處理定位權限請求失敗結果
     */
    private fun handleLocationPermissionDenied() {
        with((activity as? MainActivity)) {
            this?.showSnackBar(
                message = getString(R.string.sentence_location_permission_denied),
                actionText = getString(R.string.word_confirm)
            ) { openAppSettings() }
        }
    }

    /**
     * 處理人氣餐廳卡片模式
     */
    private fun handleDrawCardMode(@DrawCardMode mode: Int) = with(binding) {
        tvPopular.text = when (mode) {
            DrawCardMode.NEAREST -> getString(R.string.sentence_near_popular_restaurant)

            DrawCardMode.FAVORITE -> getString(R.string.sentence_favorite_popular_restaurant)

            else -> getString(R.string.sentence_near_popular_restaurant)
        }
    }

    /**
     * 處理當前的定位點資訊
     */
    private fun handleMyPlace(
        list: List<MyPlaceResult>,
        placeId: String
    ) = with(binding) {
        tvLocation.text = list.find { it.placeId == placeId }?.name
            ?: getString(R.string.sentence_near_restaurant)
    }

    /**
     * 處理儲存的定位點資訊結果
     */
    private fun handleMyPlaceListResult(
        event: Event<NetworkResult<EmptyNetworkResult>>
    ) = with(viewModel) {
        event.getContentIfNotHandled?.handleResult {
            onLoading = { setLoading(true) }
            onSuccess = {
                myPlaceList.value.find { it.placeId == myPlaceId.value }
                    ?.let { place -> updateLocationAndDrawCard(place.lat, place.lng) }
                    ?: getCurrentLocation(
                        onSuccess = ::updateLocationAndDrawCard,
                        onFailure = { displayToast(getString(R.string.sentence_can_not_get_location)) }
                    )
            }
            onError = { _, msg ->
                setLoading(false)
                displayToast(msg ?: "Unknown Error")
            }
        }
    }

    /**
     * 取得當前的座標經緯度
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onSuccess: (lat: Double, lng: Double) -> Unit,
        onFailure: () -> Unit
    ) {
        if (checkLocationPermissionAndGPS()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    // 如果 lastLocation 為 null，請求新的位置更新
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.locations.lastOrNull()?.let { latestLocation ->
                                onSuccess(latestLocation.latitude, latestLocation.longitude)
                                // 取得一次即移除更新
                                fusedLocationClient.removeLocationUpdates(this)
                            } ?: onFailure()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                }
            }.addOnFailureListener {
                Timber.e(message = "取得經緯度失敗", t = it)
                onFailure()
            }
        }
    }

    /**
     * 更新座標並取得人氣餐廳卡片
     */
    private fun updateLocationAndDrawCard(lat: Double, lng: Double) {
        viewModel.currentLat = lat
        viewModel.currentLng = lng
        viewModel.getDrawCard()
    }

    /**
     * 處理人氣餐廳卡片結果
     */
    private fun handleDrawCardResult(
        event: Event<NetworkResult<List<RestaurantResult>>>
    ) {
        handleBasicResult(event,
            workOnSuccess = { it?.let(viewModel::setDrawCardList) },
            workOnError = { viewModel.setDrawCardList(emptyList()) }
        )
    }

    /**
     * 處理人氣餐廳卡片列表
     */
    private fun handleDrawCardList(
        list: List<RestaurantResult>
    ) = with(binding) {
        rlRv.isVisible = list.isNotEmpty()
        lottieNoData.isVisible = list.isEmpty()
        drawCardAdapter.submitList(list) { rvPopular.post { rvPopular.smoothScrollToPosition(0) } }
    }

    companion object {
        /**
         * 選擇定位點 Dialog
         */
        private const val REQUEST_CODE_SELECT_PLACE = "REQUEST_CODE_SELECT_PLACE"
    }
}