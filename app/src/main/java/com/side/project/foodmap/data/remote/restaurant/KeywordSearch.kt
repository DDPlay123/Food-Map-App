package com.side.project.foodmap.data.remote.restaurant

import com.side.project.foodmap.data.remote.*

data class KeywordSearchReq(
    override val accessKey: String,
    override val userId: String,
    val location: Location,
    val distance: Int = 100,
    val keyword: String,
    val skip: Int = 20,
    val limit: Int = 100
) : BaseRequest()

class KeywordSearchRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String? = null,
        val updated: Boolean,
        val placeCount: Int,
        val placeList: ArrayList<PlaceList>
    )
}

