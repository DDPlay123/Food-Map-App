package mai.project.foodmap.features.restaurant_feature.restaurantListScreen

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
import mai.project.core.Configs
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
class RestaurantListViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo,
    preferenceRepo: PreferenceRepo
) : BaseViewModel(contextProvider) {

    var totalRestaurantCount = 0
        private set

    // region State
    /**
     * 是否顯示距離控制器
     */
    private val _showDistanceController = MutableStateFlow(false)
    val showDistanceController = _showDistanceController.asStateFlow()

    fun toggleShowDistanceController() {
        _showDistanceController.value = !_showDistanceController.value
    }

    /**
     * 搜尋的距離 (公里)
     */
    private val _searchDistance = MutableStateFlow(Configs.MIN_SEARCH_DISTANCE)
    val searchDistance = _searchDistance.asStateFlow()

    fun setSearchDistance(distance: Int) {
        _searchDistance.value = distance
    }

    /**
     * 當前的餐廳列表資料
     */
    private val _restaurantList = MutableStateFlow<List<RestaurantResult>>(emptyList())
    val restaurantList = _restaurantList.asStateFlow()

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
     * 餐廳搜尋結果
     */
    private val _searchRestaurantsResult = MutableStateFlow<Event<NetworkResult<List<RestaurantResult>>>>(Event(NetworkResult.Idle()))
    val searchRestaurantsResult = _searchRestaurantsResult.asStateFlow()

    fun searchRestaurants(
        keyword: String,
        lat: Double,
        lng: Double,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT
    ) = launchCoroutineIO {
        val distance = searchDistance.value * 1000
        if (skip == DEFAULT_SKIP) {
            _restaurantList.update { emptyList() }
        }
        safeApiCallFlow {
            if (keyword.isNotEmpty()) {
                placeRepo.searchPlacesByKeyword(keyword, lat, lng, distance, skip, limit)
            } else {
                placeRepo.searchPlacesByDistance(lat, lng, distance, skip, limit)
            }
        }.collect { result ->
            _searchRestaurantsResult.update { Event(result) }
            when (result) {
                is NetworkResult.Success -> {
                    result.data?.let { newList ->
                        totalRestaurantCount = if (newList.isNotEmpty()) newList.first().placeCount else 0
                        _restaurantList.update { it + newList }
                    }
                }

                else -> Unit
            }
        }
    }
    // endregion Network State

    companion object {
        /**
         * 預設的 skip
         */
        const val DEFAULT_SKIP = 0

        /**
         * 預設的 limit
         */
        const val DEFAULT_LIMIT = 50
    }
}