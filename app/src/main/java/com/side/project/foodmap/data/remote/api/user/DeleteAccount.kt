package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

class DeleteAccountReq : BaseRequest()

data class DeleteAccountRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}