package com.side.project.foodmap.ui.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.data.remote.api.restaurant.DetailsByPlaceIdReq
import com.side.project.foodmap.data.remote.api.restaurant.DetailsByPlaceIdRes
import com.side.project.foodmap.data.remote.api.user.PushFavoriteReq
import com.side.project.foodmap.data.remote.api.user.PushFavoriteRes
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailViewModel : BaseViewModel() {

    init {
        getAccessKeyFromDataStore()
        getUserUIDFromDataStore()
    }
    /**
     * 資料流
     */
    private val _searchDetailState = MutableStateFlow<Resource<DetailsByPlaceIdRes>>(Resource.Unspecified())
    val searchDetailState
        get() = _searchDetailState.asStateFlow()

    private val _pushFavoriteState = MutableStateFlow<Resource<PushFavoriteRes>>(Resource.Unspecified())
    val pushFavoriteState
        get() = _pushFavoriteState.asStateFlow()

    /**
     * 可呼叫方法
     */
    fun searchDetail(placeId: String) {
        val detailsByPlaceIdReq = DetailsByPlaceIdReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            place_id = placeId
        )
        viewModelScope.launch { _searchDetailState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiDetailByPlaceId(detailsByPlaceIdReq).enqueue(object : Callback<DetailsByPlaceIdRes> {
            override fun onResponse(
                call: Call<DetailsByPlaceIdRes>,
                @NonNull response: Response<DetailsByPlaceIdRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        _searchDetailState.value = Resource.Success(it)
                    }
                }
            }

            override fun onFailure(call: Call<DetailsByPlaceIdRes>, t: Throwable) {
                viewModelScope.launch {
                    _searchDetailState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun pushFavorite(favoriteList: ArrayList<FavoriteList>) {
        val pushFavoriteReq = PushFavoriteReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            favoriteList = favoriteList
        )
        ApiClient.getAPI.apiPushFavorite(pushFavoriteReq).enqueue(object : Callback<PushFavoriteRes> {
            override fun onResponse(
                call: Call<PushFavoriteRes>,
                response: Response<PushFavoriteRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> _pushFavoriteState.value = Resource.Success(it)
                            else -> _pushFavoriteState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PushFavoriteRes>, t: Throwable) {
                viewModelScope.launch {
                    _pushFavoriteState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }
}