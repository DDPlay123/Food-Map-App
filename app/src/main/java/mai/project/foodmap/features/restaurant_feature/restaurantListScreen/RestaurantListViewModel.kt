package mai.project.foodmap.features.restaurant_feature.restaurantListScreen

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mai.project.core.utils.CoroutineContextProvider
import mai.project.foodmap.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class RestaurantListViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider
) : BaseViewModel(contextProvider) {

    // region State
    /**
     * 是否顯示距離控制器
     */
    private val _showDistanceController = MutableStateFlow(false)
    val showDistanceController = _showDistanceController.asStateFlow()

    fun toggleShowDistanceController() {
        _showDistanceController.value = !_showDistanceController.value
    }
    // endregion State
}