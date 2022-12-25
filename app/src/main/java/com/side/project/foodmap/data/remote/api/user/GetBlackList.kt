package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse
import com.side.project.foodmap.data.remote.api.PlaceList

data class GetBlackListReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

data class GetBlackListRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val placeCount: Int,
        val placeList: ArrayList<PlaceList>
    )
}