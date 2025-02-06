package mai.project.foodmap.features.home_features.mapTabScreen

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentMapTabBinding

@AndroidEntryPoint
class MapTabFragment : BaseFragment<FragmentMapTabBinding, MapTabViewModel>(
    bindingInflater = FragmentMapTabBinding::inflate
) {
    override val viewModel by viewModels<MapTabViewModel>()

    override val useActivityOnBackPressed: Boolean = true
}