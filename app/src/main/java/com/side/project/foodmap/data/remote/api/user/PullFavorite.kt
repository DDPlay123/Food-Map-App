package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse

data class PullFavoriteReq(
    override val accessKey: String,
    override val userId: String,
    val favoriteIdList: ArrayList<String>
) : BaseRequest()

data class PullFavoriteRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}