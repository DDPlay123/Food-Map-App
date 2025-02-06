package mai.project.foodmap.features.home_features.profilesTabScreen

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.utils.Method
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentProfilesTabBinding

@AndroidEntryPoint
class ProfilesTabFragment : BaseFragment<FragmentProfilesTabBinding, ProfilesTabViewModel>(
    bindingInflater = FragmentProfilesTabBinding::inflate
) {
    override val viewModel by viewModels<ProfilesTabViewModel>()

    override val useActivityOnBackPressed: Boolean = true

    override fun FragmentProfilesTabBinding.initialize(savedInstanceState: Bundle?) {

    }

    override fun FragmentProfilesTabBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 使用者大頭貼
            { userImage.collect { imgAvatar.setImageBitmap(Method.decodeImage(it)) } },
            // 使用者名稱
            { userName.collect { tvUsername.text = it } },
        )
    }

    override fun FragmentProfilesTabBinding.setListener() {

    }
}