package mai.project.foodmap.features.home_features.mapTabScreen

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class MapTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo,
    preferenceRepo: PreferenceRepo
) : BaseViewModel(contextProvider) {

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
    // endregion Network State
}