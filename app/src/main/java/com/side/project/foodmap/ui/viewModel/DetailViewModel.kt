package com.side.project.foodmap.ui.viewModel

import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.SetLocation

class DetailViewModel : BaseViewModel() {

    /**
     * 參數
     */
    var decodePolylineArray: List<LatLng> = emptyList()

    /**
     * 資料流
     */
    val placeDetailFlow get() = restaurantApiRepo.placeDetailFlow

    val getRoutePolylineFlow get() = geocodeApiRepo.getRoutePolylineFlow

    val pushBlackListFlow get() = userApiRepo.pushBlackListFlow

    val pullBlackListFlow get() = userApiRepo.pullBlackListFlow

    val pushFavoriteListFlow get() = userApiRepo.pushFavoriteListFlow

    val pullFavoriteListFlow get() = userApiRepo.pullFavoriteListFlow

    /**
     * 可呼叫方法
     */
    fun searchDetail(
        placeId: String
    ) = restaurantApiRepo.apiDetailByPlaceId(placeId)

    fun getPolyLine(
        origin: SetLocation,
        destination: SetLocation
    ) = geocodeApiRepo.apiGeocodeGetRoutePolyline(origin, destination)

    fun pushBlackList(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPushBlackList(placeIdList)

    fun pullBlackList(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPullBlackList(placeIdList)

    fun pushFavorite(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPushFavorite(placeIdList)

    fun pullFavorite(
        placeIdList: ArrayList<String>
    ) = userApiRepo.apiPullFavorite(placeIdList)
}