package mai.project.foodmap.features.home_features.mapTabScreen

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.data.annotations.ThemeMode
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.models.RestaurantRouteResult
import mai.project.foodmap.domain.repository.GeocodeRepo
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class MapTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo,
    private val geocodeRepo: GeocodeRepo,
    private val userRepo: UserRepo,
    preferenceRepo: PreferenceRepo
) : BaseViewModel(contextProvider) {

    // 是否已經移動到我的位置
    var isMoveCameraToMyLocation = false

    // 當前 RecyclerView 停止的 Item
    var currentItem: RestaurantResult? = null

    // 路徑點
    var routePoints: List<LatLng> = emptyList()

    // region State
    /**
     * 當前的餐廳列表
     */
    private val _restaurantList = MutableStateFlow<List<RestaurantResult>>(emptyList())
    val restaurantList = _restaurantList.asStateFlow()

    fun setRestaurantList(list: List<RestaurantResult>) {
        _restaurantList.update { list }
    }
    // endregion State

    // region Local State
    /**
     * 顯示模式
     */
    val themeMode: StateFlow<Int> = preferenceRepo.readThemeMode
        .distinctUntilChanged()
        .catch { emit(ThemeMode.SYSTEM) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, ThemeMode.SYSTEM)

    /**
     * 儲存的收藏清單 PlaceId
     */
    val myFavoritePlaceIdList: StateFlow<Set<String>> = preferenceRepo.readMyFavoritePlaceIds
        .distinctUntilChanged()
        .catch { emit(emptySet()) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, emptySet())

    /**
     * 儲存的黑名單 PlaceId
     */
    val myBlacklistPlaceIdList: StateFlow<Set<String>> = preferenceRepo.readMyBlacklistPlaceIds
        .distinctUntilChanged()
        .catch { emit(emptySet()) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, emptySet())
    // endregion Local State

    // region Network State
    /**
     * 取得附近地區的餐廳資訊
     */
    private val _nearbyRestaurant = MutableStateFlow<Event<NetworkResult<List<RestaurantResult>>>>(Event(NetworkResult.Idle()))
    val nearbyRestaurant = _nearbyRestaurant.asStateFlow()
    
    fun getNearbyRestaurant(
        lat: Double, 
        lng: Double,
        distance: Int
    ) = launchCoroutineIO {
        safeApiCallFlow {
            placeRepo.searchPlacesByDistance(
                lat = lat,
                lng = lng,
                distance = distance,
                // 先寫死
                skip = 0,
                limit = 50
            )
        }.collect { result -> _nearbyRestaurant.update { Event(result) } }
    }

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
     * 新增/移除 收藏
     */
    private val _pushOrPullMyFavoriteResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val pushOrPullMyFavoriteResult = _pushOrPullMyFavoriteResult.asStateFlow()

    fun pushOrPullMyFavorite(
        placeId: String,
        isFavorite: Boolean
    ) = launchCoroutineIO {
        safeApiCallFlow {
            userRepo.pushOrPullMyFavorite(placeId, isFavorite)
        }.collect { result -> _pushOrPullMyFavoriteResult.update { Event(result) } }
    }
    // endregion Network State

    init {
        launchCoroutineIO { themeMode.collect() }
    }
}