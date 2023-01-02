package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.*
import com.side.project.foodmap.data.remote.restaurant.DistanceSearchRes
import com.side.project.foodmap.data.remote.restaurant.DrawCardRes
import com.side.project.foodmap.util.RegisterLoginFieldsState
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    init {
        getUserUIDFromDataStore()
        getUserNameFromDataStore()
        getUserPictureFromDataStore()
    }
    /**
     * 暫存
     */


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

    /**
     * 資料流
     */
    // Home Page
    val putFcmTokenFlow get() = userApiRepo.putFcmTokenFlow

    val drawCardData get() = restaurantApiRepo.drawCardFlow

    val distanceSearchFlow get() = restaurantApiRepo.distanceSearchFlow

    val autoCompleteFlow get() = restaurantApiRepo.autoCompleteFlow

    private var _historySearchList = MutableLiveData<List<AutoComplete>>()
    val historySearchList: LiveData<List<AutoComplete>> get() = _historySearchList

    val syncPlaceListData: LiveData<List<MyPlaceList>> get() = userApiRepo.getSyncPlaceListData

    val pullPlaceListFlow get() = userApiRepo.pullPlaceListFlow

    val pushBlackListFlow get() = userApiRepo.pushBlackListFlow

    val pullBlackListFlow get() = userApiRepo.pullBlackListFlow

    // Favorite Page
    val syncFavoriteListData: LiveData<List<FavoriteList>> get() = userApiRepo.getSyncFavoriteListData

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
    fun putFcmToken(
        fcmToken: String
    ) = userApiRepo.apiAddFcmToken(fcmToken)

    fun drawCard(
        location: Location,
        isRecent: Boolean,
        num: Int = 10
    ) {
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