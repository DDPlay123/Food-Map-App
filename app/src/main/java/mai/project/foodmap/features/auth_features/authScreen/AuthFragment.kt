package mai.project.foodmap.features.auth_features.authScreen

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.utils.Event
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.data.annotations.StatusCode
import mai.project.foodmap.databinding.FragmentAuthBinding
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.state.ValidationResult
import mai.project.foodmap.domain.utils.handleResult

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding, AuthViewModel>(
    bindingInflater = FragmentAuthBinding::inflate
) {
    override val viewModel by viewModels<AuthViewModel>()

    override val useKeyboardListener: Boolean = true

    override fun FragmentAuthBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // Loading 狀態
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 輸入結果驗證
            { authValidation.collect(::handleAuthValidation) },
            // 登入狀態
            { loginResult.collect(::handleLoginAndRegisterResult) },
            // 註冊狀態
            { registerResult.collect(::handleLoginAndRegisterResult) }
        )
    }

    override fun FragmentAuthBinding.setListener() {
        btnStart.onClick {
            viewModel.login(
                edUsername.text?.trim().toString(),
                edPassword.text?.trim().toString(),
                checkbox.isChecked
            )
        }
    }

    /**
     * 處理使用者名稱、密碼輸入驗證結果
     */
    private fun handleAuthValidation(state: AuthFieldsState) = with(binding) {
        if (state.username is ValidationResult.Failure) {
            edUsername.error = getString(state.username.stringRes)
        }

        if (state.password is ValidationResult.Failure) {
            edPassword.error = getString(state.password.stringRes)
        }
    }

    /**
     * 處理登入或註冊結果
     */
    private fun handleLoginAndRegisterResult(
        event: Event<NetworkResult<EmptyNetworkResult>>
    ) = with(viewModel) {
        event.getContentIfNotHandled?.handleResult {
            onLoading = { setLoading(true) }
            onSuccess = { setLoading(false) }
            onError = { data, msg ->
                setLoading(false)
                data?.status?.let { handleLoginAndRegisterError(it, msg ?: "Unknown Error") }
            }
        }
    }

    /**
     * 處理登入或註冊失敗
     */
    private fun handleLoginAndRegisterError(
        @StatusCode statusCode: Int,
        errMsg: String
    ) {
        when (statusCode) {
            StatusCode.ACCOUNT_NOT_EXIST -> {
                // TODO 詢問是否註冊帳號
            }

            else -> displayToast(errMsg)
        }
    }
}