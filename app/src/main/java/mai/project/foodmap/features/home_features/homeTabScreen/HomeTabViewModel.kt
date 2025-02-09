package mai.project.foodmap.features.home_features.homeTabScreen

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.data.annotations.DrawCardMode
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val placeRepo: PlaceRepo
) : BaseViewModel(contextProvider) {

    // region Network State
    /**
     * 抽取人氣餐廳卡片
     */
    private val _drawCardResult = MutableStateFlow<Event<NetworkResult<List<RestaurantResult>>>>(Event(NetworkResult.Idle()))
    val drawCardResult = _drawCardResult.asStateFlow()

    fun getDrawCard(
        lat: Double,
        lng: Double,
        isNearest: Boolean
    ) = launchCoroutineIO {
        safeApiCallFlow {
            placeRepo.getDrawCard(lat, lng, if (isNearest) DrawCardMode.NEAREST else DrawCardMode.FAVORITE)
        }.collect { result -> _drawCardResult.update { Event(result) } }
    }
    // endregion Network State
}