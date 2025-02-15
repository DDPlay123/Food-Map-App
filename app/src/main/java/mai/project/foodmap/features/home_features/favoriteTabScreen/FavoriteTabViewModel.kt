package mai.project.foodmap.features.home_features.favoriteTabScreen

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
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class FavoriteTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val userRepo: UserRepo
) : BaseViewModel(contextProvider) {

    // 選中的項目 (地址)
    var selectedItemByAddress: MyFavoriteResult? = null

    // region Local State
    /**
     * 儲存的收藏清單
     */
    val myFavorites: StateFlow<List<MyFavoriteResult>> = userRepo.getMyFavoriteList
        .distinctUntilChanged()
        .catch { emit(emptyList()) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, emptyList())
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