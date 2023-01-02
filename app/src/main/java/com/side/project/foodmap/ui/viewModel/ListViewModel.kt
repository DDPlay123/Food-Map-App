package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.MyPlaceList
import com.side.project.foodmap.data.remote.PlaceList
import com.side.project.foodmap.util.Resource

class ListViewModel : BaseViewModel() {

    /**
     * 參數
     */
    // 實際要觀察的資料(附近/關鍵字)
    var searchData: MutableList<PlaceList> = ArrayList()

    // 設定區域
    var regionPlaceId: String = ""
    var isUseMyLocation: Boolean = true
    var selectLatLng: Location = Location(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)

    // 資料參數
    lateinit var listType: String
    lateinit var keyword: String

    var settingDistance: Int = 1000
    var alreadyCalledNum: Int = 0
    var repeatNum: Int = 0
    var totalCount: Int = 0

    /**
     * 資料流
     */
    val syncPlaceListData: LiveData<List<MyPlaceList>> get() = userApiRepo.getSyncPlaceListData

    val distanceSearchFlow = restaurantApiRepo.distanceSearchFlow

    val keywordSearchFlow = restaurantApiRepo.keywordSearchFlow

    private val _observeSearchData = MutableLiveData<Resource<MutableList<PlaceList>>>()
    val observeSearchData: LiveData<Resource<MutableList<PlaceList>>>
        get() = _observeSearchData

    val pushFavoriteFlow get() = userApiRepo.pushFavoriteListFlow

    val pullFavoriteFlow get() = userApiRepo.pullFavoriteListFlow

    val getSyncBlackListData get() = userApiRepo.getSyncBlackListData

    /**
     * 可呼叫方法
     */
    fun getSyncPlaceList() =
        userApiRepo.apiSyncPlaceList()

    fun distanceSearch(
        location: Location,
        distance: Int = -1,
        skip: Int = 0,
        limit: Int = 50
    ) = restaurantApiRepo.apiRestaurantDistanceSearch(location, distance, skip, limit)

    fun keywordSearch(
        location: Location,
        distance: Int,
        keyword: String,
        skip: Int,
        limit: Int
    ) = restaurantApiRepo.apiRestaurantKeywordSearch(location, distance, keyword, skip, limit)

    fun pushFavorite(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPushFavorite(placeIdList)

    fun pullFavorite(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPullFavorite(placeIdList)

    fun getSyncBlackList() =
        userApiRepo.apiSyncBlackList()

    fun setObserveSearchData(placeList: ArrayList<PlaceList>) {
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