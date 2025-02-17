package mai.project.foodmap.features.restaurant_feature.restaurantListScreen

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class RestaurantListViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo
) : BaseViewModel(contextProvider) {

    // region State
    /**
     * 是否顯示距離控制器
     */
    private val _showDistanceController = MutableStateFlow(false)
    val showDistanceController = _showDistanceController.asStateFlow()

    fun toggleShowDistanceController() {
        _showDistanceController.value = !_showDistanceController.value
    }
    // endregion State

    // region Network State
    /**
     * 餐廳搜尋結果
     */
    private val _searchRestaurantsResult = MutableStateFlow<Event<NetworkResult<List<RestaurantResult>>>>(Event(NetworkResult.Idle()))
    val searchRestaurantsResult = _searchRestaurantsResult.asStateFlow()

    fun searchRestaurants(
        keyword: String,
        lat: Double,
        lng: Double,
        distance: Int,
        skip: Int,
        limit: Int
    ) = launchCoroutineIO {
        safeApiCallFlow {
            if (keyword.isNotEmpty()) {
                placeRepo.searchPlacesByKeyword(keyword, lat, lng, distance, skip, limit)
            } else {
                placeRepo.searchPlacesByDistance(lat, lng, distance, skip, limit)
            }
        }.collect { result -> _searchRestaurantsResult.update { Event(result) } }
    }
    // endregion Network State
}