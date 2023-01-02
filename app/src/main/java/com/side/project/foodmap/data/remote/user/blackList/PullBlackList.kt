package com.side.project.foodmap.data.remote.user.blackList

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.Info

data class PullBlackListReq(
    override val accessKey: String,
    override val userId: String,
    val placeIdList: ArrayList<String>
) : BaseRequest()

data class PullBlackListRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String,
        val info: Info
    )
}