package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.*

data class DistanceSearchReq(
    override val accessKey: String,
    override val userId: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int = 100,
    val minNum: Int = 20,
    val maxNum: Int = 100
) : BaseRequest()

class DistanceSearchRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(
        val updated: Boolean,
        val placeCount: Long,
        val placeList: ArrayList<PlaceList>
    )
}

