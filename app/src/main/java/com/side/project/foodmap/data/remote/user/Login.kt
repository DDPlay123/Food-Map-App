package com.side.project.foodmap.data.remote.user

import com.side.project.foodmap.data.remote.BaseResponse

data class LoginReq(
    val username: String,
    val password: String,
    val deviceId: String
)

data class LoginRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String,
        val userId: String,
        val accessKey: String
    )
}