package mai.project.foodmap.features.auth_features.authScreen

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import mai.project.core.Configs
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.ValidationResult
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val userRepo: UserRepo,
    preferenceRepo: PreferenceRepo
) : BaseViewModel(contextProvider) {

    val savedAccount = preferenceRepo.readAccount
    val savedPassword = preferenceRepo.readPassword

    // region State
    /**
     * 驗證欄位是否正確
     */
    private val _authValidation = Channel<AuthFieldsState>()
    val authValidation = _authValidation.receiveAsFlow()

    // endregion State

    // region Network State
    /**
     * 登入
     */
    private val _loginResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val loginResult = _loginResult.asStateFlow()

    fun login(
        username: String,
        password: String,
        isRemember: Boolean,
    ) = launchCoroutineIO {
        val usernameResult = validateUsername(username)
        val passwordResult = validatePassword(password)

        if (usernameResult is ValidationResult.Success && passwordResult is ValidationResult.Success) {
            safeApiCallFlow { userRepo.login(username, password, isRemember) }
                .collect { result -> _loginResult.update { Event(result) } }
        } else {
            _authValidation.send(
                AuthFieldsState(username = usernameResult, password = passwordResult)
            )
        }
    }

    /**
     * 註冊
     */
    private val _registerResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val registerResult = _registerResult.asStateFlow()

    fun register(
        username: String,
        password: String,
        isRemember: Boolean,
    ) = launchCoroutineIO {
        val usernameResult = validateUsername(username)
        val passwordResult = validatePassword(password)

        if (usernameResult is ValidationResult.Success && passwordResult is ValidationResult.Success) {
            safeApiCallFlow { userRepo.register(username, password, isRemember) }
                .collect { result -> _registerResult.update { Event(result) } }
        } else {
            _authValidation.send(
                AuthFieldsState(username = usernameResult, password = passwordResult)
            )
        }
    }
    // endregion Network State

    // region support method
    /**
     * 驗證使用者名稱是否輸入正確
     */
    private fun validateUsername(username: String): ValidationResult {
        return when {
            username.isEmpty() -> ValidationResult.Failure(R.string.rule_username_empty)

            username.length < Configs.USERNAME_LENGTH_MIN ->
                ValidationResult.Failure(R.string.rule_username_length)

            username.length > Configs.USERNAME_LENGTH_MAX ->
                ValidationResult.Failure(R.string.rule_username_limit)

            !username.matches(Regex(Configs.USERNAME_FORMATER)) ->
                ValidationResult.Failure(R.string.rule_username_format)

            else -> ValidationResult.Success
        }
    }

    /**
     * 驗證密碼是否輸入正確
     */
    private fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Failure(R.string.rule_password_empty)

            password.length < Configs.PASSWORD_LENGTH_MIN ->
                ValidationResult.Failure(R.string.rule_password_length)

            password.length > Configs.PASSWORD_LENGTH_MAX ->
                ValidationResult.Failure(R.string.rule_password_limit)

            else -> ValidationResult.Success
        }
    }
    // endregion support method
}