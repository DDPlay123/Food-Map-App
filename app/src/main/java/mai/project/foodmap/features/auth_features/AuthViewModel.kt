package mai.project.foodmap.features.auth_features

import com.google.firebase.installations.FirebaseInstallations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.NetworkResult
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.utils.safeApiCall
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val userRepo: UserRepo
) : BaseViewModel(contextProvider) {

    // region Network State
    private val _loginResult = MutableStateFlow<Event<NetworkResult<Nothing>>>(Event(NetworkResult.Idle()))
    val loginResult = _loginResult.asStateFlow()

    fun login(
        username: String,
        password: String
    ) {
        _loginResult.update { Event(NetworkResult.Loading()) }
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) launchCoroutineIO {
                val result = safeApiCall { userRepo.login(username, password, task.result.orEmpty()) }
                _loginResult.update { Event(result) }
            } else {
                _loginResult.update { Event(NetworkResult.Error(message = "Firebase Error")) }
            }
        }
    }
    // endregion
}