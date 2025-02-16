package mai.project.foodmap.features.restaurant_feature.restaurantListScreen

import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentRestaurantListBinding

@AndroidEntryPoint
class RestaurantListFragment : BaseFragment<FragmentRestaurantListBinding, RestaurantListViewModel>(
    bindingInflater = FragmentRestaurantListBinding::inflate
) {
    override val viewModel by viewModels<RestaurantListViewModel>()

    private val args by navArgs<RestaurantListFragmentArgs>()

    override fun FragmentRestaurantListBinding.initialize(savedInstanceState: Bundle?) {
        tvTitle.text = when (args.type) {
            is ListType.BlackList -> getString(R.string.sentence_black_list)
            is ListType.KeywordSearch -> (args.type as ListType.KeywordSearch).keyword
            is ListType.DistanceSearch -> getString(R.string.sentence_near_restaurant)
        }
        imgDistance.isInvisible = args.type is ListType.BlackList
    }

    override fun FragmentRestaurantListBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 是否顯示距離控制器
            { showDistanceController.collect { clController.isVisible = it } }
        )
    }

    override fun FragmentRestaurantListBinding.setListener() {
        imgBack.onClick { navigateUp() }

        imgDistance.onClick(safe = false) { viewModel.toggleShowDistanceController() }

        swipeRefresh.setOnRefreshListener {
            // TODO
            swipeRefresh.isRefreshing = false
        }
    }
}