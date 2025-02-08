package mai.project.foodmap.features.auth_features.authScreen

import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.extensions.parcelable
import mai.project.core.utils.Event
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.data.annotations.StatusCode
import mai.project.foodmap.databinding.FragmentAuthBinding
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.state.ValidationResult
import mai.project.foodmap.domain.utils.handleResult
import mai.project.foodmap.features.dialogs_features.prompt.PromptCallback

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding, AuthViewModel>(
    bindingInflater = FragmentAuthBinding::inflate
) {
    override val viewModel by viewModels<AuthViewModel>()

    override val useKeyboardListener: Boolean = true

    override fun FragmentAuthBinding.initialize(savedInstanceState: Bundle?) {
        edUsername.setText(viewModel.savedAccount)
        edPassword.setText(viewModel.savedPassword)
        checkbox.isChecked = viewModel.savedAccount.isNotEmpty() && viewModel.savedPassword.isNotEmpty()
    }

    override fun FragmentAuthBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 輸入結果驗證
            { authValidation.collect(::handleAuthValidation) },
            // 登入狀態
            { loginResult.collect { handleLoginAndRegisterResult(true, it) } },
            // 註冊狀態
            { registerResult.collect { handleLoginAndRegisterResult(false, it) } }
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

    override fun FragmentAuthBinding.setCallback() {
        setFragmentResultListener(REQUEST_CODE_REGISTER_HINT) { _, bundle ->
            bundle.parcelable<PromptCallback>(PromptCallback.ARG_CONFIRM)?.let {
                viewModel.register(
                    edUsername.text?.trim().toString(),
                    edPassword.text?.trim().toString(),
                    checkbox.isChecked
                )
            }
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
        isRegister: Boolean,
        event: Event<NetworkResult<EmptyNetworkResult>>
    ) = with(binding) {
        event.getContentIfNotHandled?.handleResult {
            onLoading = { navigateLoadingDialog(isOpen = true, cancelable = false) }
            onSuccess = {
                navigateLoadingDialog(isOpen = false)
                if (isRegister) viewModel.login(
                    edUsername.text?.trim().toString(),
                    edPassword.text?.trim().toString(),
                    checkbox.isChecked
                )
            }
            onError = { data, msg ->
                navigateLoadingDialog(isOpen = false)
                handleLoginAndRegisterError(data?.status, msg ?: "Unknown Error")
            }
        }
    }

    /**
     * 處理登入或註冊失敗
     */
    private fun handleLoginAndRegisterError(
        @StatusCode statusCode: Int?,
        errMsg: String
    ) {
        when (statusCode) {
            StatusCode.ACCOUNT_NOT_EXIST -> {
                navigatePromptDialog(
                    requestCode = REQUEST_CODE_REGISTER_HINT,
                    title = getString(R.string.sentence_new_user),
                    message = getString(R.string.sentence_register_prompt)
                )
            }

            else -> displayToast(errMsg)
        }
    }

    companion object {
        /**
         * 提示是否要註冊帳號的 Dialog
         */
        private const val REQUEST_CODE_REGISTER_HINT = "REQUEST_CODE_REGISTER_HINT"
    }
}