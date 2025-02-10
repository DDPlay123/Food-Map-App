package mai.project.foodmap.features.home_features.mapTabScreen

import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentMapTabBinding

@AndroidEntryPoint
class MapTabFragment : BaseFragment<FragmentMapTabBinding, MapTabViewModel>(
    bindingInflater = FragmentMapTabBinding::inflate
) {
    override val viewModel by hiltNavGraphViewModels<MapTabViewModel>(R.id.nav_main)

    override val useActivityOnBackPressed: Boolean = true
}