package mai.project.foodmap.features.home_features.homeTabScreen

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import mai.project.core.Configs
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.extensions.parcelable
import mai.project.core.extensions.screenWidth
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.core.widget.recyclerView_decorations.ScaleItemDecoration
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.checkGPS
import mai.project.foodmap.base.checkGPSAndGetCurrentLocation
import mai.project.foodmap.base.checkLocationPermission
import mai.project.foodmap.base.checkLocationPermissionAndGPS
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateLoadingDialog
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

    override val isNavigationVisible: Boolean = true

    override val useActivityOnBackPressed: Boolean = true

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val drawCardAdapter by lazy { DrawCardAdapter() }

    private val snapHelper by lazy { LinearSnapHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher = googleMapUtil.createLocationPermissionLauncher(
            fragment = this,
            onGranted = {
                if (checkGPS(googleMapUtil) && viewModel.drawCardList.value.isEmpty())
                    viewModel.fetchMyPlaceList()
            },
            onDenied = { checkLocationPermission(googleMapUtil) }
        )
    }

    override fun FragmentHomeTabBinding.initialize(savedInstanceState: Bundle?) {
        googleMapUtil.launchLocationPermission(locationPermissionLauncher)

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
            // 新增/移除收藏
            { pushOrPullMyFavoriteResult.collect { handleBasicResult(it, false) } },
            // 人氣餐廳資料列表
            {
                drawCardList.combine(myFavoritePlaceIdList) { list, placeIds ->
                    list.map { it.copy(isFavorite = it.placeId in placeIds) }
                }.collect(::handleDrawCardList)
            }
        )
    }

    override fun FragmentHomeTabBinding.setListener() {
        tvLocation.onClick(anim = true) {
            if (checkLocationPermissionAndGPS(googleMapUtil)) {
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

        tvPopular.onClick(anim = true) {
            if (checkLocationPermissionAndGPS(googleMapUtil))
                viewModel.setDrawCardMode()
        }

        imgRefresh.onClick(anim = true) {
            if (checkLocationPermissionAndGPS(googleMapUtil))
                viewModel.fetchMyPlaceList()
        }

        cardMore.onClick {
            navigate(
                HomeTabFragmentDirections.actionHomeTabFragmentToRestaurantListFragment(
                    keyword = ""
                )
            )
        }

        drawCardAdapter.onItemClick = { item ->
            navigate(
                HomeTabFragmentDirections.actionHomeTabFragmentToRestaurantDetailFragment(
                    placeId = item.placeId,
                    name = item.name,
                    lat = item.lat.toFloat(),
                    lng = item.lng.toFloat()
                )
            )
        }

        drawCardAdapter.onFavoriteClick = { item ->
            viewModel.setFavoriteForDrawCard(item.placeId, !item.isFavorite)
        }

        rvPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager ?: return
                    val snapView = snapHelper.findSnapView(layoutManager)
                    viewModel.drawCardPosition = if (snapView != null) layoutManager.getPosition(snapView) else 0
                    Timber.d("DrawCard 目前的位置：${viewModel.drawCardPosition}")
                }
            }
        })
    }

    override fun FragmentHomeTabBinding.setCallback() {
        setFragmentResultListener(REQUEST_CODE_SELECT_PLACE) { _, bundle ->
            bundle.parcelable<MyPlaceCallback>(MyPlaceCallback.ARG_ITEM_CLICK)?.let tag@{ callback ->
                callback as MyPlaceCallback.OnItemClick
                if (viewModel.myPlaceId.value == callback.placeId) return@tag
                viewModel.setMyPlaceId(callback.placeId)
                if (checkLocationPermission(googleMapUtil)) {
                    checkPlaceIdAndFetchDrawCard(callback.placeId)
                }
            }
            bundle.parcelable<MyPlaceCallback>(MyPlaceCallback.ARG_ADD_ADDRESS)?.let {
                popBackStack(R.id.homeTabFragment, false)
                navigate(
                    HomeTabFragmentDirections.actionHomeTabFragmentToAddPlaceFragment(
                        requestCode = REQUEST_CODE_ADD_PLACE
                    )
                )
            }
        }
        setFragmentResultListener(REQUEST_CODE_ADD_PLACE) { _, bundle ->
            bundle.getString(REQUEST_CODE_ADD_PLACE)?.let {
                if (checkLocationPermissionAndGPS(googleMapUtil))
                    viewModel.fetchMyPlaceList()
            }
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
            onSuccess = { checkPlaceIdAndFetchDrawCard(myPlaceId.value) }
            onError = { _, msg ->
                setLoading(false)
                displayToast(msg ?: "Unknown Error")
            }
        }
    }

    /**
     * 檢查 PlaceId 並更新座標，最後取得人氣餐廳卡片
     */
    private fun checkPlaceIdAndFetchDrawCard(placeId: String) {
        viewModel.myPlaceList.value.find { it.placeId == placeId }
            ?.let { place -> updateLocationAndDrawCard(place.lat, place.lng) }
            ?: run {
                checkGPSAndGetCurrentLocation(
                    googleMapUtil = googleMapUtil,
                    onSuccess = ::updateLocationAndDrawCard,
                    onFailure = { updateLocationAndDrawCard(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
                )
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
    ) {
        checkGPSAndGetCurrentLocation(
            googleMapUtil = googleMapUtil,
            onSuccess = { lat, lng -> refreshDrawCardList(list, LatLng(lat, lng)) },
            onFailure = { refreshDrawCardList(list, null) }
        )
    }

    /**
     * 刷新人氣餐廳卡片列表
     */
    private fun refreshDrawCardList(
        list: List<RestaurantResult>,
        latLng: LatLng?
    ) = with(binding) {
        rlRv.isVisible = list.isNotEmpty()
        lottieNoData.isVisible = list.isEmpty()
        drawCardAdapter.submitList(list, latLng) {
            if (viewModel.drawCardPosition != -1) {
                val position = if (viewModel.drawCardPosition < list.size) viewModel.drawCardPosition else 0
                rvPopular.post {
                    smoothScrollToPositionWithOffset(position)
                }
            }
        }
    }

    /**
     * 平滑滾動 RecyclerView 並帶有偏移值
     */
    private fun smoothScrollToPositionWithOffset(
        targetPosition: Int
    ) {
        val layoutManager = binding.rvPopular.layoutManager as LinearLayoutManager
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

    companion object {
        /**
         * 選擇定位點 Dialog
         */
        private const val REQUEST_CODE_SELECT_PLACE = "REQUEST_CODE_SELECT_PLACE"

        /**
         * 新增定位點 Fragment
         */
        private const val REQUEST_CODE_ADD_PLACE = "REQUEST_CODE_ADD_PLACE"
    }
}