package mai.project.foodmap.features.home_features.profilesTabScreen

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
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.repository.UserRepo
import javax.inject.Inject

@HiltViewModel
class ProfilesTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    preferenceRepo: PreferenceRepo,
    private val userRepo: UserRepo
) : BaseViewModel(contextProvider) {

    // region Preference State
    /**
     * 使用者大頭貼
     */
    val userImage: StateFlow<String> = preferenceRepo.readUserImage
        .distinctUntilChanged()
        .catch { emit("") }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, "")

    /**
     * 使用者名稱
     */
    val userName: StateFlow<String> = preferenceRepo.readUsername
        .distinctUntilChanged()
        .catch { emit("") }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, "")
    // endregion Preference State

    // region Network State
    /**
     * 登出
     */
    private val _logoutResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val logoutResult = _logoutResult.asStateFlow()

    fun logout() = launchCoroutineIO {
        safeApiCallFlow { userRepo.logout() }
            .collect { result -> _logoutResult.update { Event(result) } }
    }
    // endregion Network State
}