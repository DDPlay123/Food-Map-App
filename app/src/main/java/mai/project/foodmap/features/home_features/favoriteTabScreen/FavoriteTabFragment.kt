package mai.project.foodmap.features.home_features.favoriteTabScreen

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentFavoriteTabBinding

@AndroidEntryPoint
class FavoriteTabFragment : BaseFragment<FragmentFavoriteTabBinding, FavoriteTabViewModel>(
    bindingInflater = FragmentFavoriteTabBinding::inflate
) {
    override val viewModel by viewModels<FavoriteTabViewModel>()

    override val useActivityOnBackPressed: Boolean = true
}