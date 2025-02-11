package mai.project.foodmap.features.restaurant_feature.restaurantListScreen

import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentRestaurantListBinding

@AndroidEntryPoint
class RestaurantListFragment : BaseFragment<FragmentRestaurantListBinding, RestaurantListViewModel>(
    bindingInflater = FragmentRestaurantListBinding::inflate
) {

}