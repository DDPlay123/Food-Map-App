package mai.project.foodmap.features.home_features.profilesTabScreen

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.repository.UserRepo
import javax.inject.Inject

@HiltViewModel
class ProfilesTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val userRepo: UserRepo
) : BaseViewModel(contextProvider) {

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