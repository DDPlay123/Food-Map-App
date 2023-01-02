package com.side.project.foodmap.data.remote.user.placeList

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.Info

data class PullPlaceListReq(
    override val accessKey: String,
    override val userId: String,
    val place_id: String
) : BaseRequest()

data class PullPlaceListRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String,
        val info: Info
    )
}