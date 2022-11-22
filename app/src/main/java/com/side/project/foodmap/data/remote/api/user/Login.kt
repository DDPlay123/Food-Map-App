package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseResponse

data class LoginReq(
    val username: String,
    val password: String,
    val deviceId: String
)

data class LoginRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String, val userId: String, val accessKey: String)
}