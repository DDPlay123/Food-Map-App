package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.api.user.AddFcmTokenReq
import com.side.project.foodmap.data.remote.api.user.AddFcmTokenRes
import com.side.project.foodmap.data.remote.api.user.GetUserImageReq
import com.side.project.foodmap.data.remote.api.user.GetUserImageRes
import com.side.project.foodmap.data.remote.google.placesSearch.PlacesSearch
import com.side.project.foodmap.helper.getLocation
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : BaseViewModel() {

    /**
     * 資料流
     */
    private val _putFcmTokenState = MutableStateFlow<Resource<AddFcmTokenRes>>(Resource.Unspecified())
    val putFcmTokenState
        get() = _putFcmTokenState.asStateFlow()

    private val _getUserImageState = MutableStateFlow<Resource<GetUserImageRes>>(Resource.Unspecified())
    val getUserImageState
        get() = _getUserImageState.asStateFlow()

    private val _placeSearchState = MutableStateFlow<Resource<PlacesSearch>>(Resource.Unspecified())
    val placeSearchState
        get() = _placeSearchState.asSharedFlow()

    private val _placeSearch = MutableLiveData<PlacesSearch>()
    val placeSearch: LiveData<PlacesSearch>
        get() = _placeSearch

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

    fun getUserImage() {
        val getUserImageReq = GetUserImageReq(
            accessKey = accessKey.value,
            userId = userUID.value,
        )
        viewModelScope.launch { _putFcmTokenState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiGetUserImage(getUserImageReq).enqueue(object : Callback<GetUserImageRes> {
            override fun onResponse(
                call: Call<GetUserImageRes>,
                response: Response<GetUserImageRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> _getUserImageState.value = Resource.Success(it)
                            else -> _getUserImageState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetUserImageRes>, t: Throwable) {
                viewModelScope.launch {
                    _getUserImageState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun placesSearch(region: String, latLng: String, key: String) {
        val currentLatLng = if (region.getLocation().first == 0.00) latLng
        else "${region.getLocation().first},${region.getLocation().second}"

        viewModelScope.launch { _placeSearchState.emit(Resource.Loading()) }
        ApiClient.googlePlaces.getPlaceSearch(
            location = currentLatLng, key = key
        ).enqueue(object : Callback<PlacesSearch> {
            override fun onResponse(call: Call<PlacesSearch>, response: Response<PlacesSearch>) {
                viewModelScope.launch {
                    response.body()?.let {
                        postPlaceSearchValue(it)
                        _placeSearchState.value = Resource.Success(it)
                    }
                }
            }

            override fun onFailure(call: Call<PlacesSearch>, t: Throwable) {
                viewModelScope.launch {
                    _placeSearchState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun postPlaceSearchValue(placesSearch: PlacesSearch) {
        _placeSearch.postValue(placesSearch)
    }
}