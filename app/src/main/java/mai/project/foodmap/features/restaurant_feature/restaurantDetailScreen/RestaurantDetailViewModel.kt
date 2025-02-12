package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.RestaurantDetailResult
import mai.project.foodmap.domain.models.RestaurantRouteResult
import mai.project.foodmap.domain.repository.GeocodeRepo
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo,
    private val geocodeRepo: GeocodeRepo
) : BaseViewModel(contextProvider) {

    // 地圖中心點位置
    var targetCameraPosition: CameraPosition? = null

    // 路徑點
    var routePoints: List<LatLng> = emptyList()

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
    // endregion Network State
}