package com.side.project.foodmap.data.remote.user.favoriteList

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.Info

data class PullFavoriteReq(
    override val accessKey: String,
    override val userId: String,
    val favoriteIdList: ArrayList<String>
) : BaseRequest()

data class PullFavoriteRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val msg: String,
        val info: Info
    )
}