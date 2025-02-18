package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.data.annotations.ThemeMode
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.RestaurantDetailResult
import mai.project.foodmap.domain.models.RestaurantRouteResult
import mai.project.foodmap.domain.repository.GeocodeRepo
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo,
    private val geocodeRepo: GeocodeRepo,
    private val userRepo: UserRepo,
    preferenceRepo: PreferenceRepo
) : BaseViewModel(contextProvider) {

    // 地圖中心點位置
    var targetCameraPosition: CameraPosition? = null

    // 路徑點
    var routePoints: List<LatLng> = emptyList()

    // region State
    /**
     * 收藏狀態
     */
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    fun setIsFavorite(isFavorite: Boolean) {
        _isFavorite.update { isFavorite }
    }

    /**
     * 黑名單狀態
     */
    private val _isBlocked = MutableStateFlow(false)
    val isBlocked = _isBlocked.asStateFlow()

    fun setIsBlocked(isBlocked: Boolean) {
        _isBlocked.update { isBlocked }
    }
    // endregion State

    // region Local State
    /**
     * 顯示模式
     */
    val themeMode: SharedFlow<Int> = preferenceRepo.readThemeMode
        .distinctUntilChanged()
        .catch { emit(ThemeMode.SYSTEM) }
        .flowOn(contextProvider.io)
        .shareIn(viewModelScope, WhileSubscribedOrRetained, 0)
    // endregion Local State

    // region Network State
    /**
     * 取得目標地的路線
     */
    private val _routeResult = MutableStateFlow<Event<NetworkResult<RestaurantRouteResult>>>(Event(NetworkResult.Idle()))
    val routeResult = _routeResult.asStateFlow()

    fun getRouteResult(
        placeId: String,
        originLat: Double,
        originLng: Double
    ) = launchCoroutineIO {
        safeApiCallFlow {
            geocodeRepo.getRoute(
                originLat = originLat,
                originLng = originLng,
                targetPlaceId = placeId
            )
        }.collect { result -> _routeResult.update { Event(result) } }
    }

    /**
     * 取得餐廳詳細資訊
     */
    private val _restaurantDetail = MutableStateFlow<Event<NetworkResult<RestaurantDetailResult>>>(Event(NetworkResult.Idle()))
    val restaurantDetail = _restaurantDetail.asStateFlow()

    fun getRestaurantDetail(
        placeId: String
    ) = launchCoroutineIO {
        safeApiCallFlow {
            placeRepo.getPlaceDetail(placeId)
        }.collect { result -> _restaurantDetail.update { Event(result) } }
    }

    /**
     * 新增/移除 收藏
     */
    private val _pushOrPullMyFavoriteResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val pushOrPullMyFavoriteResult = _pushOrPullMyFavoriteResult.asStateFlow()

    fun pushOrPullMyFavorite(
        placeId: String,
        isFavorite: Boolean
    ) = launchCoroutineIO {
        setIsFavorite(isFavorite)
        safeApiCallFlow {
            userRepo.pushOrPullMyFavorite(placeId, isFavorite)
        }.collect { result -> _pushOrPullMyFavoriteResult.update { Event(result) } }
    }

    /**
     * 新增/移除 黑名單
     */
    private val _pushOrPullMyBlackListResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val pushOrPullMyBlackListResult = _pushOrPullMyBlackListResult.asStateFlow()

    fun pushOrPullMyBlackList(
        placeId: String,
        isBlocked: Boolean
    ) = launchCoroutineIO {
        setIsBlocked(isBlocked)
        safeApiCallFlow {
            userRepo.pushOrPullMyBlocked(placeId, isBlocked)
        }.collect { result -> _pushOrPullMyBlackListResult.update { Event(result) } }
    }
    // endregion Network State
}