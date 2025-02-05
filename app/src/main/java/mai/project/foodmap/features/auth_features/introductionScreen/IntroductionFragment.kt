package mai.project.foodmap.features.auth_features.introductionScreen

import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.onClick
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentIntroductionBinding
import mai.project.foodmap.features.auth_features.AuthViewModel

@AndroidEntryPoint
class IntroductionFragment : BaseFragment<FragmentIntroductionBinding, AuthViewModel>(
    bindingInflater = FragmentIntroductionBinding::inflate
) {
    override val viewModel by hiltNavGraphViewModels<AuthViewModel>(R.id.nav_auth)

    override val useActivityOnBackPressed: Boolean = true

    override fun FragmentIntroductionBinding.setListener() {
        btnNext.onClick { navigate(R.id.action_introductionFragment_to_authFragment) }
    }
}