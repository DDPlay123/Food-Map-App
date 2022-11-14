package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

data class SetPasswordReq(
    override val accessKey: String,
    override val userId: String,
    val password: String
) : BaseRequest()

data class SetPasswordRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}