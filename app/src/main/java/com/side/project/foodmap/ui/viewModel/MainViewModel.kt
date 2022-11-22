package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchReq
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.data.remote.api.user.*
import com.side.project.foodmap.data.remote.google.placesSearch.PlacesSearch
import com.side.project.foodmap.helper.getLocation
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : BaseViewModel() {

    init {
        getAccessKeyFromDataStore()
        getDeviceId()
        getUserUIDFromDataStore()
        getUserRegionFromDataStore()
        getUserNameFromDataStore()
        getUserPictureFromDataStore()
    }

    /**
     * 資料流
     */
    // Home Page
    private val _putFcmTokenState = MutableStateFlow<Resource<AddFcmTokenRes>>(Resource.Unspecified())
    val putFcmTokenState
        get() = _putFcmTokenState.asStateFlow()

    private val _popularSearchState = MutableStateFlow<Resource<PlacesSearch>>(Resource.Unspecified())
    val popularSearchState
        get() = _popularSearchState.asStateFlow()

    private val _nearSearchState = MutableStateFlow<Resource<DistanceSearchRes>>(Resource.Unspecified())
    val nearSearchState
        get() = _nearSearchState.asStateFlow()

    val _watchDetailState = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val watchDetailState
        get() = _watchDetailState.asStateFlow()

    // Profile Page
    private val _logoutState = MutableStateFlow<Resource<LogoutRes>>(Resource.Unspecified())
    val logoutState
        get() = _logoutState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<Resource<DeleteAccountRes>>(Resource.Unspecified())
    val deleteAccountState
        get() = _deleteAccountState.asStateFlow()

    private val _setUserImageState = MutableStateFlow<Resource<SetUserImageRes>>(Resource.Unspecified())
    val setUserImageState
        get() = _setUserImageState.asStateFlow()

    /**
     * 可呼叫方法
     */
    fun putFcmToken(fcmToken: String) {
        val addFcmTokenReq = AddFcmTokenReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            deviceId = deviceId.value,
            fcmToken = fcmToken
        )
        viewModelScope.launch { _putFcmTokenState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiAddFcmToken(addFcmTokenReq).enqueue(object : Callback<AddFcmTokenRes> {
            override fun onResponse(
                call: Call<AddFcmTokenRes>,
                response: Response<AddFcmTokenRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> _putFcmTokenState.value = Resource.Success(it)
                            else -> _putFcmTokenState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AddFcmTokenRes>, t: Throwable) {
                viewModelScope.launch {
                    _putFcmTokenState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun popularSearch(region: String, latLng: String) {
        val currentLatLng = if (region.getLocation().first == 0.00) latLng
        else "${region.getLocation().first},${region.getLocation().second}"
        // TODO(人氣餐廳)

    }

    fun nearSearch(region: String, latLng: LatLng) {
        val currentLatLng = Method.getCurrentLatLng(region, latLng)
        val distanceSearchReq = DistanceSearchReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            latitude = currentLatLng.latitude,
            longitude = currentLatLng.longitude,
        )
        viewModelScope.launch { _nearSearchState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiRestaurantDistanceSearch(distanceSearchReq).enqueue(object : Callback<DistanceSearchRes> {
            override fun onResponse(call: Call<DistanceSearchRes>, response: Response<DistanceSearchRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        _nearSearchState.value = when (it.status) {
                            0 -> Resource.Success(it)
                            else -> Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DistanceSearchRes>, t: Throwable) {
                viewModelScope.launch {
                    _nearSearchState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun watchDetail(placeId: String) {
        if (placeId.isNotEmpty())
            viewModelScope.launch { _watchDetailState.emit(Resource.Success(placeId)) }
        else
            viewModelScope.launch { _watchDetailState.emit(Resource.Error("")) }
    }

    fun logout() {
        val logoutReq = LogoutReq(
            userId = userUID.value,
            accessKey = accessKey.value,
            deviceId = deviceId.value
        )
        viewModelScope.launch { _logoutState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiUserLogout(logoutReq).enqueue(object : Callback<LogoutRes> {
            override fun onResponse(call: Call<LogoutRes>, response: Response<LogoutRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _logoutState.value = Resource.Success(it)
                                putUserIsLogin(false)
                            }
                            else -> _logoutState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<LogoutRes>, t: Throwable) {
                viewModelScope.launch {
                    _logoutState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun deleteAccount() {
        val deleteAccountReq = DeleteAccountReq(
            userId = userUID.value,
            accessKey = accessKey.value
        )
        viewModelScope.launch { _deleteAccountState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiDeleteAccount(deleteAccountReq).enqueue(object : Callback<DeleteAccountRes> {
            override fun onResponse(call: Call<DeleteAccountRes>, response: Response<DeleteAccountRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _deleteAccountState.value = Resource.Success(it)
                                clearData()
                                clearPublicData()
                                deleteDistanceSearchData()
                            }
                            else -> _deleteAccountState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DeleteAccountRes>, t: Throwable) {
                viewModelScope.launch {
                    _deleteAccountState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun setUserImage(userImage: String) {
        val setUserImageReq = SetUserImageReq(
            userId = userUID.value,
            accessKey = accessKey.value,
            userImage = userImage
        )
        viewModelScope.launch { _setUserImageState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiSetUserImage(setUserImageReq).enqueue(object : Callback<SetUserImageRes> {
            override fun onResponse(call: Call<SetUserImageRes>, response: Response<SetUserImageRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _setUserImageState.value = Resource.Success(it)
                                putUserPicture(userImage)
                            }
                            else -> _setUserImageState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SetUserImageRes>, t: Throwable) {
                viewModelScope.launch {
                    _setUserImageState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }
}