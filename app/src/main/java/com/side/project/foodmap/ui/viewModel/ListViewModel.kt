package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchReq
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListViewModel : BaseViewModel() {

    init {
        getAccessKeyFromDataStore()
        getUserUIDFromDataStore()
    }
    /**
     * 資料流
     */
    val _watchDetailState = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val watchDetailState
        get() = _watchDetailState.asStateFlow()

    private val _nearSearchState = MutableStateFlow<Resource<DistanceSearchRes>>(Resource.Unspecified())
    val nearSearchState
        get() = _nearSearchState.asStateFlow()

    /**
     * 可呼叫方法
     */
    fun watchDetail(placeId: String) {
        if (placeId.isNotEmpty())
            viewModelScope.launch { _watchDetailState.emit(Resource.Success(placeId)) }
        else
            viewModelScope.launch { _watchDetailState.emit(Resource.Error("")) }
    }

    fun nearSearch(region: String, latLng: LatLng, distance: Int = 5000, minNum: Int = 20, maxNum: Int = 50) {
        val currentLatLng = Method.getCurrentLatLng(region, latLng)
        val distanceSearchReq = DistanceSearchReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            latitude = currentLatLng.latitude,
            longitude = currentLatLng.longitude,
            distance = distance,
            minNum = minNum,
            maxNum = maxNum
        )
        viewModelScope.launch { _nearSearchState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiRestaurantDistanceSearch(distanceSearchReq).enqueue(object :
            Callback<DistanceSearchRes> {
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

    fun keywordSearch() {

    }
}