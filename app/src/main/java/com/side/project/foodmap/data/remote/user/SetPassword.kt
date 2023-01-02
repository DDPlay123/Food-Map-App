package com.side.project.foodmap.data.remote.user

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse

data class SetPasswordReq(
    override val accessKey: String,
    override val userId: String,
    val password: String
) : BaseRequest()

data class SetPasswordRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String
    )
}