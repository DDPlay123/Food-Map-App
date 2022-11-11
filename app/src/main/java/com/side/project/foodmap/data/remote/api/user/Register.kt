package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseResponse

data class RegisterReq(
    val username: String,
    val password: String,
    val image: String
)

data class RegisterRes(
    val result: Result? = null
) : BaseResponse() {
    class Result(val msg: String, val userId: String)
}