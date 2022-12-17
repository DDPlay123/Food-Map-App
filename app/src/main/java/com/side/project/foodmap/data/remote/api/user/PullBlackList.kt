package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

data class PullBlackListReq(
    override val accessKey: String,
    override val userId: String,
    val placeIdList: ArrayList<String>
) : BaseRequest()

data class PullBlackListRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}