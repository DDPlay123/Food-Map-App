package com.side.project.foodmap.data.remote.restaurant

import com.side.project.foodmap.data.remote.*

data class AutoCompleteReq(
    override val accessKey: String,
    override val userId: String,
    val location: Location,
    val distance: Long, // 公尺
    val input: String
) : BaseRequest()

data class AutoCompleteRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val updated: Boolean,
        val placeCount: Int,
        val placeList: ArrayList<AutoComplete>
    )
}