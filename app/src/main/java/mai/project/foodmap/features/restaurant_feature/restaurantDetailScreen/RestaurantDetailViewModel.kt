package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import dagger.hilt.android.lifecycle.HiltViewModel
import mai.project.core.utils.CoroutineContextProvider
import mai.project.foodmap.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider
) : BaseViewModel(contextProvider) {
}