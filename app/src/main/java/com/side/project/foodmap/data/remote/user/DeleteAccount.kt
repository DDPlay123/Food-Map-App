package com.side.project.foodmap.data.remote.user

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse

data class DeleteAccountReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

data class DeleteAccountRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String
    )
}