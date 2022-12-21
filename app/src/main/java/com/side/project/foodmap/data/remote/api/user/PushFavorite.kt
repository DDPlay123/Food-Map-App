package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse
import com.side.project.foodmap.data.remote.api.Info

data class PushFavoriteReq(
    override val accessKey: String,
    override val userId: String,
    val favoriteList: ArrayList<String>
) : BaseRequest()

data class PushFavoriteRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(
        val msg: String,
        val info: Info
    )
}