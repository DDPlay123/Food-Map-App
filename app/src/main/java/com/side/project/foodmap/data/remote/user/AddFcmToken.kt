package com.side.project.foodmap.data.remote.user

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse

data class AddFcmTokenReq(
    override val accessKey: String,
    override val userId: String,
    val deviceId: String,
    val fcmToken: String
) : BaseRequest()

data class AddFcmTokenRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String
        )
}