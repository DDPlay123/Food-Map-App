package mai.project.foodmap.features.auth_features.authScreen

import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentAuthBinding
import mai.project.foodmap.features.auth_features.AuthViewModel

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding, AuthViewModel>(
    bindingInflater = FragmentAuthBinding::inflate
) {
    override val viewModel by hiltNavGraphViewModels<AuthViewModel>(R.id.nav_auth)

    override val useKeyboardListener: Boolean = true

    override fun FragmentAuthBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 登入狀態
            { loginResult.collect() }
        )
    }

    override fun FragmentAuthBinding.setListener() {
        btnStart.onClick {
            viewModel.login(
                edUsername.text?.trim().toString(),
                edPassword.text?.trim().toString()
            )
        }
    }
}