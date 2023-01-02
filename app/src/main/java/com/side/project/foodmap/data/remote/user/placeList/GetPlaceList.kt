package com.side.project.foodmap.data.remote.user.placeList

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.MyPlaceList

data class GetPlaceListReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

data class GetPlaceListRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val placeCount: Int,
        val placeList: ArrayList<MyPlaceList>
    )
}