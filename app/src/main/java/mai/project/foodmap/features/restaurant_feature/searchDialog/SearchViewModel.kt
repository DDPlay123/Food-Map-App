package mai.project.foodmap.features.restaurant_feature.searchDialog

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import mai.project.core.Configs
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.models.SearchRestaurantResult
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo,
    userRepo: UserRepo,
    preferenceRepo: PreferenceRepo
) : BaseViewModel(contextProvider) {

    var searchLatLng = LatLng(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE)

    // region State
    /**
     * 搜尋的距離 (公里)
     */
    private val _searchDistance = MutableStateFlow(Configs.MIN_SEARCH_DISTANCE)
    val searchDistance = _searchDistance.asStateFlow()

    fun setSearchDistance(distance: Int) {
        _searchDistance.value = distance
    }

    /**
     * 關鍵資搜尋
     */
    private val _searchFlow = MutableSharedFlow<String>()
    val searchFlow = _searchFlow.asSharedFlow()

    fun setSearchKeyword(keyword: String) = launchCoroutineIO {
        _searchFlow.emit(keyword)
    }

    /**
     * 搜尋結果/搜尋紀錄 列表資料
     */
    private val _restaurantList = MutableStateFlow<List<SearchRestaurantResult>>(emptyList())
    val restaurantList = _restaurantList.asStateFlow()

    fun setRestaurantList(list: List<SearchRestaurantResult>) {
        _restaurantList.value = list
    }
    // endregion State

    // region Local State
    /**
     * 當前選擇的定位點
     */
    val myPlaceId: SharedFlow<String> = preferenceRepo.readMyPlaceId
        .distinctUntilChanged()
        .catch { emit("") }
        .flowOn(contextProvider.io)
        .shareIn(viewModelScope, WhileSubscribedOrRetained, 0)

    /**
     * 儲存的定位點列表資料
     */
    val myPlaceList: SharedFlow<List<MyPlaceResult>> = userRepo.getMyPlaceList
        .distinctUntilChanged()
        .catch { emit(emptyList()) }
        .flowOn(contextProvider.io)
        .shareIn(viewModelScope, WhileSubscribedOrRetained, 0)

    /**
     * 取得本地的搜尋紀錄
     */
    fun getSearchRecords() = launchCoroutineIO {
        _restaurantList.update { placeRepo.getMySearchRecord.firstOrNull()?.reversed() ?: emptyList() }
    }

    /**
     * 新增搜尋紀錄
     */
    fun addNewSearchRecord(item: SearchRestaurantResult) = launchCoroutineIO {
        placeRepo.addNewSearchRecord(item)
    }

    /**
     * 刪除搜尋紀錄
     */
    fun deleteSearchRecord(item: SearchRestaurantResult) = launchCoroutineIO {
        placeRepo.deleteSearchRecord(item)
        _restaurantList.update { list -> list.filter { it.placeId != item.placeId } }
    }

    /**
     * 清空搜尋紀錄
     */
    fun clearAllSearchRecord() = launchCoroutineIO {
        placeRepo.deleteAllSearchRecord()
        _restaurantList.update { emptyList() }
    }
    // endregion Local State

    // region Network State
    /**
     * 搜尋相關的餐廳
     */
    private val _searchRestaurantsResult = MutableStateFlow<Event<NetworkResult<List<SearchRestaurantResult>>>>(Event(NetworkResult.Idle()))
    val searchRestaurantsResult = _searchRestaurantsResult.asStateFlow()

    fun searchRestaurants(
        keyword: String
    ) = launchCoroutineIO {
        safeApiCallFlow {
            placeRepo.searchSamePlacesByKeyword(
                keyword = keyword,
                lat = searchLatLng.latitude,
                lng = searchLatLng.longitude,
                distance = searchDistance.value * 1000
            )
        }.collect { result -> _searchRestaurantsResult.update { Event(result) } }
    }
    // endregion Network State
}