package mai.project.foodmap.features.home_features.favoriteTabScreen

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class FavoriteTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val userRepo: UserRepo,
    preferenceRepo: PreferenceRepo
) : BaseViewModel(contextProvider) {

    // 選中的項目 (地址)
    var selectedItemByAddress: MyFavoriteResult? = null

    // region State
    /**
     * 當前的收藏清單 (避免移除除收藏後，直接消失，所以獨立出來)
     */
    private val _myFavoriteList = MutableStateFlow<List<MyFavoriteResult>>(emptyList())
    val myFavoriteList = _myFavoriteList.asStateFlow()

    fun setFavoriteForList(
        placeId: String,
        isFavorite: Boolean
    ) {
        pushOrPullMyFavorite(placeId, isFavorite)
        _myFavoriteList.update { list ->
            list.map {
                if (it.placeId == placeId) it.copy(isFavorite = isFavorite) else it
            }
        }
    }
    // endregion State

    // region Local State
    /**
     * 儲存的收藏清單
     */
    fun getMyFavoritesByLocal() = launchCoroutineIO {
        _myFavoriteList.update { userRepo.getMyFavoriteList.firstOrNull() ?: emptyList() }
    }

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
     * 抓取儲存的收藏清單
     */
    private val _myFavoritesResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val myFavoritesResult = _myFavoritesResult.asStateFlow()

    fun fetchMyFavorites() = launchCoroutineIO {
        safeApiCallFlow {
            userRepo.fetchMyFavoriteList()
        }.collect { result -> _myFavoritesResult.update { Event(result) } }
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