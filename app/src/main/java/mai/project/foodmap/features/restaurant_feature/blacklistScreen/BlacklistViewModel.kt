package mai.project.foodmap.features.restaurant_feature.blacklistScreen

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
import mai.project.foodmap.domain.models.MyBlacklistResult
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val userRepo: UserRepo
) : BaseViewModel(contextProvider) {

    // region Local State
    /**
     * 儲存的黑名單列表
     */
    val myBlacklist: StateFlow<List<MyBlacklistResult>> = userRepo.getMyBlacklist
        .distinctUntilChanged()
        .catch { emit(emptyList()) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, emptyList())
    // endregion Local State

    // region Network State
    /**
     * 抓取儲存的黑名單
     */
    private val _myBlacklistResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val myBlacklistResult = _myBlacklistResult.asStateFlow()

    fun fetchMyBlacklist() = launchCoroutineIO {
        safeApiCallFlow {
            userRepo.fetchMyBlacklist()
        }.collect { result -> _myBlacklistResult.update { Event(result) } }
    }
    // endregion Network State
}