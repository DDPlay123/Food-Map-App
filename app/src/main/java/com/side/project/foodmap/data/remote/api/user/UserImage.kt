package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

data class SetUserImageReq(
    override val accessKey: String,
    override val userId: String,
    val userImage: String
) : BaseRequest()

data class SetUserImageRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}

data class GetUserImageReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

data class GetUserImageRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val userImage: String)
}