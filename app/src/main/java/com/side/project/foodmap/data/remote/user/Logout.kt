package com.side.project.foodmap.data.remote.user

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse

data class LogoutReq(
    override val accessKey: String,
    override val userId: String,
    val deviceId: String
) : BaseRequest()

data class LogoutRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String
    )
}