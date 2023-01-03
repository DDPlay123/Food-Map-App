package com.side.project.foodmap.data.remote.geocode

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.SetLocation

data class GetRoutePolylineReq(
    override val accessKey: String,
    override val userId: String,
    val origin: SetLocation,
    val destination: SetLocation
) : BaseRequest()

data class GetRoutePolylineRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val distanceMeters: Int, // 路徑距離(公尺)
        val duration: Int, // 估計時間(秒)
        val polyline: String // 路徑
    )
}