package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchReq
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.data.remote.api.restaurant.KeywordSearchReq
import com.side.project.foodmap.data.remote.api.restaurant.KeywordSearchRes
import com.side.project.foodmap.data.remote.api.user.PullFavoriteReq
import com.side.project.foodmap.data.remote.api.user.PullFavoriteRes
import com.side.project.foodmap.data.remote.api.user.PushFavoriteReq
import com.side.project.foodmap.data.remote.api.user.PushFavoriteRes
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.Resource
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
     * 參數
     */
    var searchData: MutableList<PlaceList> = ArrayList()

    /**
     * 資料流
     */
    private val _nearSearchState = MutableLiveData<Resource<DistanceSearchRes>>()
    val nearSearchState: LiveData<Resource<DistanceSearchRes>>
        get() = _nearSearchState

    private val _keywordSearchState = MutableLiveData<Resource<KeywordSearchRes>>()
    val keywordSearchState: LiveData<Resource<KeywordSearchRes>>
        get() = _keywordSearchState

    private val _observeSearchData = MutableLiveData<Resource<MutableList<PlaceList>>>()
    val observeSearchData: LiveData<Resource<MutableList<PlaceList>>>
        get() = _observeSearchData

    /**
     * 可呼叫方法
     */
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
        ApiClient.getAPI.apiRestaurantDistanceSearch(distanceSearchReq).enqueue(object :
            Callback<DistanceSearchRes> {
            override fun onResponse(call: Call<DistanceSearchRes>, response: Response<DistanceSearchRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _nearSearchState.postValue(Resource.Success(it))
                                setObserveSearchData(it.result.placeList, it.result.placeCount)
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

    fun keywordSearch(region: String, latLng: LatLng, distance: Int = 5000, keyword: String, skip: Int = 0, limit: Int = 50) {
        val currentLatLng = Method.getCurrentLatLng(region, latLng)
        val keywordSearchReq = KeywordSearchReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            latitude = currentLatLng.latitude,
            longitude = currentLatLng.longitude,
            distance = distance,
            keyword = keyword,
            skip = skip,
            limit = limit
        )
        viewModelScope.launch { _keywordSearchState.postValue(Resource.Loading()) }
        ApiClient.getAPI.apiRestaurantKeywordSearch(keywordSearchReq).enqueue(object :
            Callback<KeywordSearchRes> {
            override fun onResponse(call: Call<KeywordSearchRes>, response: Response<KeywordSearchRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _keywordSearchState.postValue(Resource.Success(it))
                                setObserveSearchData(it.result.placeList, it.result.placeCount)
                            }
                            else -> _keywordSearchState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<KeywordSearchRes>, t: Throwable) {
                viewModelScope.launch {
                    _keywordSearchState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun quickPushFavorite(placeIdList: ArrayList<String>) {
        val pushFavoriteReq = PushFavoriteReq(
            accessKey = accessKey.value,
            userId = userUID.value,
            favoriteList = placeIdList
        )
        ApiClient.getAPI.apiPushFavorite(pushFavoriteReq).enqueue(object : Callback<PushFavoriteRes> {
            override fun onResponse(
                call: Call<PushFavoriteRes>,
                response: Response<PushFavoriteRes>
            ) {
            }

            override fun onFailure(call: Call<PushFavoriteRes>, t: Throwable) {
            }
        })
    }

    fun quickPullFavorite(placeIdList: ArrayList<String>) {
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
            }

            override fun onFailure(call: Call<PullFavoriteRes>, t: Throwable) {
            }
        })
    }

    private fun setObserveSearchData(placeList: ArrayList<PlaceList>, totalCount: Int) {
        if (searchData.size.toLong() >= totalCount)
            if (totalCount == 0)
                _observeSearchData.value = Resource.Error("EMPTY")
            else
                _observeSearchData.value = Resource.Error("ERROR")
        else {
            searchData.addAll(placeList)
            _observeSearchData.postValue(Resource.Success(searchData))
        }
    }
}