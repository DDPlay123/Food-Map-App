package com.side.project.foodmap.data.remote.user.placeList

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.Info
import com.side.project.foodmap.data.remote.Location

data class PushPlaceListReq(
    override val accessKey: String,
    override val userId: String,
    val place_id: String,
    val name: String,
    val address: String,
    val location: Location
) : BaseRequest()

data class PushPlaceListRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String,
        val info: Info
    )
}