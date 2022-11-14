package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

data class AddFcmTokenReq(
    override val accessKey: String,
    override val userId: String,
    val deviceId: String,
    val fcmToken: String
) : BaseRequest()

data class AddFcmTokenRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}