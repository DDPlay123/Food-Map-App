package com.side.project.foodmap.data.remote.user.blackList

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.PlaceList

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