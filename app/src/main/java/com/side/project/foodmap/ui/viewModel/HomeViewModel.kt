package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchReq
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.data.remote.api.user.AddFcmTokenReq
import com.side.project.foodmap.data.remote.api.user.AddFcmTokenRes
import com.side.project.foodmap.data.remote.google.placesSearch.PlacesSearch
import com.side.project.foodmap.helper.getLocation
import com.side.project.foodmap.network.ApiClient
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

    private val _popularSearchState = MutableStateFlow<Resource<PlacesSearch>>(Resource.Unspecified())
    val popularSearchState
        get() = _popularSearchState.asStateFlow()

    private val _nearSearchState = MutableStateFlow<Resource<DistanceSearchRes>>(Resource.Unspecified())
    val nearSearchState
        get() = _nearSearchState.asStateFlow()

    val _watchDetailState = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val watchDetailState
        get() = _watchDetailState.asStateFlow()

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
        val currentLatLng = getCurrentLatLng(region, latLng)
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

    /**
     * 取得正確經緯度
     */
    private fun getCurrentLatLng(region: String, latLng: LatLng): LatLng =
        LatLng(
            if (region.getLocation().first != 0.00) region.getLocation().first else latLng.latitude,
            if (region.getLocation().second != 0.00) region.getLocation().second else latLng.longitude
        )
}