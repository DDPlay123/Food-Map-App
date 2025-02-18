package mai.project.foodmap.features.auth_features.introductionScreen

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.onClick
import mai.project.foodmap.R
import mai.project.foodmap.SharedViewModel
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentIntroductionBinding

@AndroidEntryPoint
class IntroductionFragment : BaseFragment<FragmentIntroductionBinding, SharedViewModel>(
    bindingInflater = FragmentIntroductionBinding::inflate
) {
    override val viewModel by activityViewModels<SharedViewModel>()

    override val useActivityOnBackPressed: Boolean = true

    private val args by navArgs<IntroductionFragmentArgs>()

    override fun FragmentIntroductionBinding.initialize(savedInstanceState: Bundle?) {
        pbCircular.isVisible = !args.isInitialize
        groupUI.isVisible = args.isInitialize
    }

    override fun FragmentIntroductionBinding.setListener() {
        btnNext.onClick(anim = true, isAnimEndCallback = true) {
            navigate(R.id.action_introductionFragment_to_authFragment)
        }
    }
}