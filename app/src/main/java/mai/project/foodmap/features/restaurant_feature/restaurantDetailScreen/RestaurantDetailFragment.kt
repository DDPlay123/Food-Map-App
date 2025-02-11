package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentRestaurantDetailBinding

@AndroidEntryPoint
class RestaurantDetailFragment : BaseFragment<FragmentRestaurantDetailBinding, RestaurantDetailViewModel>(
    bindingInflater = FragmentRestaurantDetailBinding::inflate
) {

}