package mai.project.foodmap.features.home_features.profilesTabScreen

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.onClick
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentProfilesTabBinding

@AndroidEntryPoint
class ProfilesTabFragment : BaseFragment<FragmentProfilesTabBinding, ProfilesTabViewModel>(
    bindingInflater = FragmentProfilesTabBinding::inflate
) {
    override val viewModel by viewModels<ProfilesTabViewModel>()

    override val useActivityOnBackPressed: Boolean = true

    override fun FragmentProfilesTabBinding.setListener() {
        btnLogout.onClick {
            viewModel.logout()
        }
    }
}