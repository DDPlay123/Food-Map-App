package com.side.project.foodmap.data.remote.user

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse

data class SetUserImageReq(
    override val accessKey: String,
    override val userId: String,
    val userImage: String
) : BaseRequest()

data class SetUserImageRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String
    )
}

data class GetUserImageReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

data class GetUserImageRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val userImage: String
    )
}