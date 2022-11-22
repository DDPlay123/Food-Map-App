package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

data class DeleteAccountReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

data class DeleteAccountRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}