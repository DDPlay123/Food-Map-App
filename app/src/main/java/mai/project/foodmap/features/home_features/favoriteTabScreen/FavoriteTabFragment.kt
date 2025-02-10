package mai.project.foodmap.features.home_features.favoriteTabScreen

import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentFavoriteTabBinding

@AndroidEntryPoint
class FavoriteTabFragment : BaseFragment<FragmentFavoriteTabBinding, FavoriteTabViewModel>(
    bindingInflater = FragmentFavoriteTabBinding::inflate
) {
    override val viewModel by hiltNavGraphViewModels<FavoriteTabViewModel>(R.id.nav_main)

    override val useActivityOnBackPressed: Boolean = true
}