package com.side.project.foodmap.ui.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.api.restaurant.DetailsByPlaceIdReq
import com.side.project.foodmap.data.remote.api.restaurant.DetailsByPlaceIdRes
import com.side.project.foodmap.data.remote.api.user.*
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

    private val _pushBlackListState = MutableStateFlow<Resource<PushBlackListRes>>(Resource.Unspecified())
    val pushBlackListState
        get() = _pushBlackListState.asStateFlow()

    private val _pullBlackListState = MutableStateFlow<Resource<PullBlackListRes>>(Resource.Unspecified())
    val pullBlackListState
        get() = _pullBlackListState.asStateFlow()

    private val _pushFavoriteState = MutableStateFlow<Resource<PushFavoriteRes>>(Resource.Unspecified())
    val pushFavoriteState
        get() = _pushFavoriteState.asStateFlow()

    private val _pullFavoriteState = MutableStateFlow<Resource<PullFavoriteRes>>(Resource.Unspecified())
    val pullFavoriteState
        get() = _pullFavoriteState.asStateFlow()

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

    fun pushBlackList(placeIdList: ArrayList<String>) {
        val pushBlackListReq = PushBlackListReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            placeIdList = placeIdList
        )
        viewModelScope.launch { _pushBlackListState.value = Resource.Loading() }
        ApiClient.getAPI.apiPushBlackList(pushBlackListReq).enqueue(object : Callback<PushBlackListRes> {
            override fun onResponse(
                call: Call<PushBlackListRes>,
                response: Response<PushBlackListRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> _pushBlackListState.value = Resource.Success(it)
                            else -> _pushBlackListState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PushBlackListRes>, t: Throwable) {
                viewModelScope.launch {
                    _pushBlackListState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun pullBlackList(placeIdList: ArrayList<String>) {
        val pullBlackListReq = PullBlackListReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            placeIdList = placeIdList
        )
        viewModelScope.launch { _pullBlackListState.value = Resource.Loading() }
        ApiClient.getAPI.apiPullBlackList(pullBlackListReq).enqueue(object : Callback<PullBlackListRes> {
            override fun onResponse(
                call: Call<PullBlackListRes>,
                response: Response<PullBlackListRes>
            ) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> _pullBlackListState.value = Resource.Success(it)
                            else -> _pullBlackListState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PullBlackListRes>, t: Throwable) {
                viewModelScope.launch {
                    _pullBlackListState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun pushFavorite(placeIdList: ArrayList<String>) {
        val pushFavoriteReq = PushFavoriteReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            favoriteList = placeIdList
        )
        viewModelScope.launch { _pushFavoriteState.value = Resource.Loading() }
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

    fun pullFavorite(placeIdList: ArrayList<String>) {
        val pullFavoriteReq = PullFavoriteReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            favoriteIdList = placeIdList
        )
        viewModelScope.launch { _pullFavoriteState.value = Resource.Loading() }
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
}