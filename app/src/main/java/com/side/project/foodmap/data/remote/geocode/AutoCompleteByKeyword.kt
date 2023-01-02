package com.side.project.foodmap.data.remote.geocode

import com.side.project.foodmap.data.remote.*

data class AutoCompleteByKeywordReq(
    override val accessKey: String,
    override val userId: String,
    val location: Location,
    val input: String
) : BaseRequest()

data class AutoCompleteByKeywordRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val updated: Boolean,
        val placeCount: Int,
        val placeList: ArrayList<AutoComplete>
    )
}