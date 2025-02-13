package mai.project.foodmap.features.home_features.homeTabScreen

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
import mai.project.foodmap.data.annotations.DrawCardMode
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val preferenceRepo: PreferenceRepo,
    private val userRepo: UserRepo,
    private val placeRepo: PlaceRepo
) : BaseViewModel(contextProvider) {

    var currentLat = Configs.DEFAULT_LATITUDE
    var currentLng = Configs.DEFAULT_LONGITUDE

    var drawCardPosition = 0

    // region State
    /**
     * 當前的 抽取人氣餐廳卡片模式
     */
    private val _drawCardMode = MutableStateFlow(DrawCardMode.NEAREST)
    val drawCardMode = _drawCardMode.asStateFlow()

    fun setDrawCardMode() {
        val mode = when (drawCardMode.value) {
            DrawCardMode.NEAREST -> DrawCardMode.FAVORITE
            DrawCardMode.FAVORITE -> DrawCardMode.NEAREST
            else -> DrawCardMode.NEAREST
        }
        _drawCardMode.update { mode }
        getDrawCard()
    }

    /**
     * 人氣餐廳資料
     */
    private val _drawCardList = MutableStateFlow<List<RestaurantResult>>(emptyList())
    val drawCardList = _drawCardList.asStateFlow()

    fun setDrawCardList(list: List<RestaurantResult>) {
        drawCardPosition = 0
        _drawCardList.update { list }
    }

    fun setFavoriteForDrawCard(
        placeId: String,
        isFavorite: Boolean
    ) {
        drawCardPosition = -1
        pushOrPullMyFavorite(placeId, isFavorite)
        _drawCardList.update { list ->
            list.map { item ->
                if (item.placeId == placeId) item.copy(isFavorite = isFavorite) else item
            }
        }
    }
    // endregion State

    // region Preference State
    /**
     * 當前選擇的定位點
     */
    val myPlaceId: StateFlow<String> = preferenceRepo.readMyPlaceId
        .distinctUntilChanged()
        .catch { emit("") }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, "")

    fun setMyPlaceId(placeId: String) = launchCoroutineIO {
        preferenceRepo.writeMyPlaceId(placeId)
    }
    // endregion Preference State

    // region Local State
    /**
     * 儲存的定位點列表資料
     */
    val myPlaceList: StateFlow<List<MyPlaceResult>> = userRepo.getMyPlaceList
        .distinctUntilChanged()
        .catch { emit(emptyList()) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, emptyList())

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
    // endregion

    // region Network State
    /**
     * 抓取儲存的定位點
     */
    private val _myPlaceListResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val myPlaceListResult = _myPlaceListResult.asStateFlow()

    fun fetchMyPlaceList() = launchCoroutineIO {
        safeApiCallFlow {
            userRepo.fetchMyPlaceList()
        }.collect { result -> _myPlaceListResult.update { Event(result) } }
    }

    /**
     * 抽取人氣餐廳卡片
     */
    private val _drawCardResult = MutableStateFlow<Event<NetworkResult<List<RestaurantResult>>>>(Event(NetworkResult.Idle()))
    val drawCardResult = _drawCardResult.asStateFlow()

    fun getDrawCard() = launchCoroutineIO {
        safeApiCallFlow {
            placeRepo.getDrawCard(currentLat, currentLng, drawCardMode.value)
        }.collect { result -> _drawCardResult.update { Event(result) } }
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
}