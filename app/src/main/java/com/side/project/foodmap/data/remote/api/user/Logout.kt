package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

data class LogoutReq(
    override val accessKey: String,
    override val userId: String,
    val deviceId: String
) : BaseRequest()

data class LogoutRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}