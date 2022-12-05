package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchReq
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.data.remote.api.restaurant.DrawCardReq
import com.side.project.foodmap.data.remote.api.restaurant.DrawCardRes
import com.side.project.foodmap.data.remote.api.user.*
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Coroutines
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Exception

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

    private val _popularSearchState = MutableLiveData<Resource<DrawCardRes>>()
    val popularSearchState: LiveData<Resource<DrawCardRes>>
        get() = _popularSearchState

    private val _nearSearchState = MutableLiveData<Resource<DistanceSearchRes>>()
    val nearSearchState: LiveData<Resource<DistanceSearchRes>>
        get() = _nearSearchState

    // Favorite Page
    val observeFavoriteListFromRoom = getFavoriteData()

    private val _getFavoriteListState = MutableLiveData<Resource<GetFavoriteRes>>()
    val getFavoriteListState: LiveData<Resource<GetFavoriteRes>>
        get() = _getFavoriteListState

    private val _pullFavoriteState = MutableStateFlow<Resource<PullFavoriteRes>>(Resource.Unspecified())
    val pullFavoriteState
        get() = _pullFavoriteState.asStateFlow()

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

    fun popularSearch(region: String, latLng: LatLng, mode: Int = 0, num: Int = 10) {
        val currentLatLng = Method.getCurrentLatLng(region, latLng)
        val drawCardReq = DrawCardReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            latitude = currentLatLng.latitude,
            longitude = currentLatLng.longitude,
            mode = mode,
            num = num
        )
        viewModelScope.launch { _popularSearchState.postValue(Resource.Loading()) }
        ApiClient.getAPI.apiDrawCard(drawCardReq).enqueue(object : Callback<DrawCardRes> {
            override fun onResponse(call: Call<DrawCardRes>, response: Response<DrawCardRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _popularSearchState.postValue(Resource.Success(it))
                                insertDrawCardData(it)
                            }
                            else -> _popularSearchState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DrawCardRes>, t: Throwable) {
                viewModelScope.launch {
                    _popularSearchState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun nearSearch(region: String, latLng: LatLng, distance: Int = 5000, skip: Int = 0, limit: Int = 50) {
        val currentLatLng = Method.getCurrentLatLng(region, latLng)
        val distanceSearchReq = DistanceSearchReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            latitude = currentLatLng.latitude,
            longitude = currentLatLng.longitude,
            distance = distance,
            skip = skip,
            limit = limit
        )
        viewModelScope.launch { _nearSearchState.postValue(Resource.Loading()) }
        ApiClient.getAPI.apiRestaurantDistanceSearch(distanceSearchReq).enqueue(object : Callback<DistanceSearchRes> {
            override fun onResponse(call: Call<DistanceSearchRes>, response: Response<DistanceSearchRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                         when (it.status) {
                            0 -> {
                                _nearSearchState.postValue(Resource.Success(it))
                                insertDistanceSearchData(it)
                            }
                            else -> _nearSearchState.value = Resource.Error(it.errMsg.toString())
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

    fun getFavoriteList() {
        val getFavoriteReq = GetFavoriteReq(
            userId = userUID.value,
            accessKey = accessKey.value
        )
        viewModelScope.launch { _getFavoriteListState.postValue(Resource.Loading()) }
        ApiClient.getAPI.apiGetFavorite(getFavoriteReq).enqueue(object : Callback<GetFavoriteRes> {
            override fun onResponse(
                call: Call<GetFavoriteRes>,
                response: Response<GetFavoriteRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> _getFavoriteListState.postValue(Resource.Success(it))
                            else ->_getFavoriteListState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetFavoriteRes>, t: Throwable) {
                viewModelScope.launch {
                    _getFavoriteListState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun pullFavorite(placeIdList: ArrayList<String>) {
        val pullFavoriteReq = PullFavoriteReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            favoriteIdList = placeIdList
        )
        ApiClient.getAPI.apiPullFavorite(pullFavoriteReq).enqueue(object : Callback<PullFavoriteRes> {
            override fun onResponse(
                call: Call<PullFavoriteRes>,
                response: Response<PullFavoriteRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> _pullFavoriteState.value = Resource.Success(it)
                            else -> _pullFavoriteState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PullFavoriteRes>, t: Throwable) {
                viewModelScope.launch {
                    _pullFavoriteState.value = Resource.Error(t.message.toString())
                }
            }
        })
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
                                _logoutState.emit(Resource.Success(it))
                                putUserIsLogin(false)
                            }
                            else -> _logoutState.emit(Resource.Error(it.errMsg.toString()))
                        }
                    }
                }
            }

            override fun onFailure(call: Call<LogoutRes>, t: Throwable) {
                viewModelScope.launch {
                    _logoutState.emit(Resource.Error(t.message.toString()))
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
                                _deleteAccountState.emit(Resource.Success(it))
                                clearData()
                                clearPublicData()
                                clearDbData()
                            }
                            else -> _deleteAccountState.emit(Resource.Error(it.errMsg.toString()))
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DeleteAccountRes>, t: Throwable) {
                viewModelScope.launch {
                    _deleteAccountState.emit(Resource.Error(t.message.toString()))
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
                                _setUserImageState.emit(Resource.Success(it))
                                putUserPicture(userImage)
                            }
                            else -> _setUserImageState.emit(Resource.Error(it.errMsg.toString()))
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SetUserImageRes>, t: Throwable) {
                viewModelScope.launch {
                    _setUserImageState.emit(Resource.Error(t.message.toString()))
                }
            }
        })
    }
}