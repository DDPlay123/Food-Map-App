package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.*

data class KeywordSearchReq(
    override val accessKey: String,
    override val userId: String,
    val latitude: Double,
    val longitude: Double,
    val keyboard: String,
    val minNum: Int,
    val maxNum: Int
) : BaseRequest()

class KeywordSearchRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(
        val updated: Boolean,
        val placeCount: Long,
        val placeList: ArrayList<PlaceList>
    )
}

