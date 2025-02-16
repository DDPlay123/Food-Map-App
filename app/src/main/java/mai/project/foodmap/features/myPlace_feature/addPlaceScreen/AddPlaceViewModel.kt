package mai.project.foodmap.features.myPlace_feature.addPlaceScreen

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import mai.project.core.Configs
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.data.annotations.ThemeMode
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.SearchPlaceResult
import mai.project.foodmap.domain.repository.GeocodeRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class AddPlaceViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val preferenceRepo: PreferenceRepo,
    private val userRepo: UserRepo,
    private val geocodeRepo: GeocodeRepo
) : BaseViewModel(contextProvider) {

    // 選擇的地區資訊
    var selectedPlace: SearchPlaceResult? = null

    // 地圖中心點位置
    var targetCameraPosition: CameraPosition? = null

    // region State
    /**
     * 是否顯示搜尋列表
     */
    private val _isShowSearchList = MutableStateFlow(false)
    val isShowSearchList = _isShowSearchList.asStateFlow()

    fun setShowSearchList(isShow: Boolean) {
        _isShowSearchList.update { isShow }
    }

    /**
     * 關鍵資搜尋
     */
    private val _searchFlow = MutableSharedFlow<String>()
    val searchFlow = _searchFlow.asSharedFlow()

    fun setSearchKeyword(keyword: String) = launchCoroutineIO { _searchFlow.emit(keyword) }
    // endregion State

    // region Local State
    /**
     * 顯示模式
     */
    val themeMode: StateFlow<Int> = preferenceRepo.readThemeMode
        .distinctUntilChanged()
        .catch { emit(ThemeMode.SYSTEM) }
        .flowOn(contextProvider.io)
        .stateIn(viewModelScope, WhileSubscribedOrRetained, ThemeMode.SYSTEM)
    // endregion Local State

    // region Network State
    /**
     * 經緯度搜尋地區資訊
     */
    private val _searchPlaceResult = MutableStateFlow<Event<NetworkResult<SearchPlaceResult>>>(Event(NetworkResult.Idle()))
    val searchPlaceResult = _searchPlaceResult.asStateFlow()

    fun searchPlacesByLocation() = launchCoroutineIO {
        val lat = targetCameraPosition?.target?.latitude ?: 0.0
        val lng = targetCameraPosition?.target?.longitude ?: 0.0
        if (lat == 0.0 || lng == 0.0) return@launchCoroutineIO
        safeApiCallFlow {
            geocodeRepo.searchPlacesByLocation(lat, lng)
        }.collect { result -> _searchPlaceResult.update { Event(result) } }
    }

    /**
     * 關鍵字和經緯度搜尋地區資訊列表 (不含經緯度)
     */
    private val _searchPlacesResult = MutableStateFlow<Event<NetworkResult<List<SearchPlaceResult>>>>(Event(NetworkResult.Idle()))
    val searchPlacesResult = _searchPlacesResult.asStateFlow()

    fun searchPlacesByKeyword(keyword: String) = launchCoroutineIO {
        val lat = targetCameraPosition?.target?.latitude ?: 0.0
        val lng = targetCameraPosition?.target?.longitude ?: 0.0
        if (lat == 0.0 || lng == 0.0) return@launchCoroutineIO
        safeApiCallFlow {
            geocodeRepo.searchPlacesByKeyword(keyword, lat, lng)
        }.collect { result -> _searchPlacesResult.update { Event(result) } }
    }

    /**
     * 關鍵字搜尋地區資訊 (含經緯度)
     */
    private val _placeDetailResult = MutableStateFlow<Event<NetworkResult<SearchPlaceResult>>>(Event(NetworkResult.Idle()))
    val placeDetailResult = _placeDetailResult.asStateFlow()

    fun getPlaceByAddress(address: String) = launchCoroutineIO {
        safeApiCallFlow {
            geocodeRepo.getPlaceByAddress(address)
        }.collect { result -> _placeDetailResult.update { Event(result) } }
    }

    /**
     * 儲存定位點
     */
    private val _pushMyPlaceResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val pushMyPlaceResult = _pushMyPlaceResult.asStateFlow()

    fun pushMyPlace() = launchCoroutineIO {
        selectedPlace?.apply {
            safeApiCallFlow {
                userRepo.pushMyPlace(
                    placeId = placeId,
                    name = name,
                    address = address,
                    lat = lat ?: Configs.DEFAULT_LATITUDE,
                    lng = lng ?: Configs.DEFAULT_LONGITUDE
                )
            }.collect { result ->
                if (result is NetworkResult.Success) {
                    preferenceRepo.writeMyPlaceId(placeId)
                }
                _pushMyPlaceResult.update { Event(result) }
            }
        }
    }
    // endregion Network State

    init {
        launchCoroutineIO { themeMode.collect() }
    }
}