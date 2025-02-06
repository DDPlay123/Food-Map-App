package mai.project.foodmap.features.home_features.homeTabScreen

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentHomeTabBinding

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding, HomeTabViewModel>(
    bindingInflater = FragmentHomeTabBinding::inflate
) {
    override val viewModel by viewModels<HomeTabViewModel>()

    override val useActivityOnBackPressed: Boolean = true
}