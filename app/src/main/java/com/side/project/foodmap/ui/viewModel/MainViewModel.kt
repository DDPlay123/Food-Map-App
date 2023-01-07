package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.*
import com.side.project.foodmap.data.remote.restaurant.DistanceSearchRes
import com.side.project.foodmap.util.RegisterLoginFieldsState
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    init {
        getUserUIDFromDataStore()
        getUserNameFromDataStore()
    }
    /**
     * 參數
     */
    // 人氣卡
    var isRecentPopularSearch: Boolean = true
    // 設定區域
    var myPlaceLists: MutableList<MyPlaceList> = ArrayList()
    var regionPosition: Int = 0
    var regionPlaceId: String = ""
    var isUseMyLocation: Boolean = true
    var selectLatLng: Location = Location(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
    var isSearchPlaceList: Boolean = false
    // 地圖，追蹤模式
    var isTrack: Boolean = false
    var mapPolylineArray: List<LatLng> = emptyList()
    var mapPolylineDistance: Int = 0 // 公尺
    var mapPolylineDuration: Int = 0 // 估計時間(秒)
    // 地圖，搜尋路線
    var distanceSearchRes: DistanceSearchRes? = null
    var index = 0
    var placeId = ""
    var placeName = ""
    var lat = DEFAULT_LATITUDE
    var lng = DEFAULT_LONGITUDE
    // 最愛清單路線
    var favoritePolylineArray: List<LatLng> = emptyList()
    var favoritePolylineDistance: Int = 0 // 公尺
    var favoritePolylineDuration: Int = 0 // 估計時間(秒)

    /**
     * 資料流
     */
    val getUserImageFlow get() = userApiRepo.getUserImageFlow

    val getRoutePolylineFlow get() = geocodeApiRepo.getRoutePolylineFlow

    // Home Page
    val putFcmTokenFlow get() = userApiRepo.putFcmTokenFlow

    val drawCardData get() = restaurantApiRepo.drawCardFlow

    val distanceSearchFlow get() = restaurantApiRepo.distanceSearchFlow

    val autoCompleteFlow get() = restaurantApiRepo.autoCompleteFlow

    private var _historySearchList = MutableLiveData<List<AutoComplete>>()
    val historySearchList: LiveData<List<AutoComplete>> get() = _historySearchList

    val syncPlaceListFlow get() = userApiRepo.getSyncPlaceListFlow

    val pullPlaceListFlow get() = userApiRepo.pullPlaceListFlow

    val pushBlackListFlow get() = userApiRepo.pushBlackListFlow

    val pullBlackListFlow get() = userApiRepo.pullBlackListFlow

    // Favorite Page
    val syncFavoriteListFlow get() = userApiRepo.getSyncFavoriteListFlow

    val pushFavoriteFlow get() = userApiRepo.pushFavoriteListFlow

    val pullFavoriteFlow get() = userApiRepo.pullFavoriteListFlow

    // Profile Page
    val logoutFlow get() = userApiRepo.logoutFlow

    val deleteAccountFlow get() = userApiRepo.deleteAccountFlow

    val setUserImageFlow get() = userApiRepo.setUserImageFlow

    val setPasswordFlow get() = userApiRepo.setUserPasswordFlow

    private val _validation = Channel<RegisterLoginFieldsState>()
    val validation
        get() = _validation.receiveAsFlow()

    /**
     * 可呼叫方法
     */
    fun getUserImage() =
        userApiRepo.apiGetUserImage()

    fun getPolyLine(
        origin: SetLocation,
        destination: SetLocation
    ) = geocodeApiRepo.apiGeocodeGetRoutePolyline(origin, destination)

    fun putFcmToken(
        fcmToken: String
    ) = userApiRepo.apiAddFcmToken(fcmToken)

    fun drawCard(
        location: Location,
        isRecent: Boolean,
        num: Int = 10
    ) {
        if (isSearchPlaceList) return
        restaurantApiRepo.apiDrawCard(location, if (isRecent) 0 else 1, num)
    }

    fun distanceSearch(
        location: Location,
        distance: Int = -1,
        skip: Int = 0,
        limit: Int = 50
    ) = restaurantApiRepo.apiRestaurantDistanceSearch(location, distance, skip, limit)

    fun autoComplete(
        location: Location,
        distance: Long,
        input: String
    ) {
        if (input.isEmpty()) return
        restaurantApiRepo.apiAutoComplete(location, distance, input)
    }

    fun getHistorySearchData() =
        _historySearchList.postValue(getHistoryData())

    fun getSyncPlaceList(isSearchPlaceList: Boolean) {
        this.isSearchPlaceList = isSearchPlaceList
        userApiRepo.apiSyncPlaceList()
    }

    fun pushPlaceList(
        place_id: String,
        name: String,
        address: String,
        location: Location
    ) = userApiRepo.apiPushPlaceList(place_id, name, address, location)

    fun pullPlaceList(
        place_id: String
    ) = userApiRepo.apiPullPlaceList(place_id)

    fun getSyncFavoriteList() =
        userApiRepo.apiSyncFavoriteList()

    fun pushFavorite(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPushFavorite(placeIdList)

    fun pullFavorite(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPullFavorite(placeIdList)

    fun setPassword(
        password: String
    ) {
        if (checkValidation(password))
            userApiRepo.apiSetUserPassword(password)
        else
            viewModelScope.launch {
                _validation.send(
                    RegisterLoginFieldsState(
                        password = Method.validatePassword(password)
                    )
                )
            }
    }

    fun logout() =
        userApiRepo.apiUserLogout()

    fun deleteAccount() =
        userApiRepo.apiDeleteAccount()

    fun setUserImage(
        userImage: String
    ) = userApiRepo.apiSetUserImage(userImage)

    /**
     * 其他方法
     */
    private fun checkValidation(password: String): Boolean {
        val validPassword = Method.validatePassword(password)
        return validPassword is RegisterLoginValidation.Success
    }
}