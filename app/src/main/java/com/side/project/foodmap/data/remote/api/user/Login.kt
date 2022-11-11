package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseResponse

data class LoginReq(
    val username: String,
    val password: String,
    val deviceId: String,
    val fcmToken: String
)

data class LoginRes(
    val result: Result? = null
) : BaseResponse() {
    class Result(val msg: String, val userId: String, val accessKey: String)
}