package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.google.placesDetails.PlacesDetails
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailViewModel : BaseViewModel() {

    /**
     * 資料流
     */
    private val _searchDetailState = MutableStateFlow<Resource<PlacesDetails>>(Resource.Unspecified())
    val searchDetailState
        get() = _searchDetailState.asStateFlow()

    /**
     * 可呼叫方法
     */
    fun searchDetail(placeId: String, key: String) {
        viewModelScope.launch { _searchDetailState.emit(Resource.Loading()) }
        ApiClient.googlePlaces.getPlaceDetails(placeId, key = key).enqueue(object : Callback<PlacesDetails> {
            override fun onResponse(call: Call<PlacesDetails>, response: Response<PlacesDetails>) {
                viewModelScope.launch {
                    response.body()?.let {
                        _searchDetailState.value = Resource.Success(it)
                    }
                }
            }

            override fun onFailure(call: Call<PlacesDetails>, t: Throwable) {
                viewModelScope.launch {
                    _searchDetailState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }
}