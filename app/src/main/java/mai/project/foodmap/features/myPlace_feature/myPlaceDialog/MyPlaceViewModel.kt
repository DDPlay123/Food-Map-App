package mai.project.foodmap.features.myPlace_feature.myPlaceDialog

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class MyPlaceViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    preferenceRepo: PreferenceRepo,
    private val userRepo: UserRepo
) : BaseViewModel(contextProvider) {

    // region Preference State
    /**
     * 當前選擇的定位點
     */
    val myPlaceId: StateFlow<String> = preferenceRepo.readMyPlaceId
        .distinctUntilChanged()
        .catch { emit("") }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, "")
    // endregion Preference State

    // region Local State
    /**
     * 儲存的定位點列表資料
     */
    val myPlaceList: StateFlow<List<MyPlaceResult>> = userRepo.getMyPlaceList
        .distinctUntilChanged()
        .catch { emit(emptyList()) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, emptyList())
    // endregion

    // region Network State
    /**
     * 抓取儲存的定位點
     */
    private val _myPlaceListResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val myPlaceListResult = _myPlaceListResult.asStateFlow()

    private fun fetchMyPlaceList() = launchCoroutineIO {
        safeApiCallFlow {
            userRepo.fetchMyPlaceList()
        }.collect { result -> _myPlaceListResult.update { Event(result) } }
    }

    /**
     * 移除定位點
     */
    private val _pullMyPlaceResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val pullMyPlaceResult = _pullMyPlaceResult.asStateFlow()

    fun pullMyPlace(placeId: String) = launchCoroutineIO {
        safeApiCallFlow {
            userRepo.pullMyPlace(placeId)
        }.collect { result -> _pullMyPlaceResult.update { Event(result) } }
    }
    // endregion Network State

    init {
        fetchMyPlaceList()
    }
}