package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.google.placesSearch.PlacesSearch
import com.side.project.foodmap.helper.getLocation
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : BaseViewModel() {
    init {
        getUserRegionFromDataStore()
        getUserPictureFromDataStore()
    }

    /**
     * 資料流
     */
    private val _placeSearchState = MutableStateFlow<Resource<PlacesSearch>>(Resource.Unspecified())
    val placeSearchState
        get() = _placeSearchState.asSharedFlow()

    private val _placeSearch = MediatorLiveData<PlacesSearch>()
    val placeSearch
        get() = _placeSearch

    /**
     * 可呼叫方法
     */
    fun placesSearch(region: String, latLng: String, key: String) {
        val currentLatLng = if (region.getLocation().first == 0.00) latLng
        else "${region.getLocation().first},${region.getLocation().second}"

        viewModelScope.launch { _placeSearchState.emit(Resource.Loading()) }
        ApiClient.googlePlaces.getPlaceSearch(
            location = currentLatLng, key = key
        ).enqueue(object : Callback<PlacesSearch> {
            override fun onResponse(call: Call<PlacesSearch>, response: Response<PlacesSearch>) {
                response.body()?.let {
                    postPlaceSearchValue(it)
                    _placeSearchState.value = Resource.Success(it)
                }
            }

            override fun onFailure(call: Call<PlacesSearch>, t: Throwable) {
                _placeSearchState.value = Resource.Error(t.message.toString())
            }
        })
    }

    fun postPlaceSearchValue(placesSearch: PlacesSearch) {
        _placeSearch.postValue(placesSearch)
    }
}