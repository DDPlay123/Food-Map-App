package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse
import com.side.project.foodmap.data.remote.api.FavoriteList

data class PushFavoriteReq(
    override val accessKey: String,
    override val userId: String,
    val favoriteList: ArrayList<FavoriteList>
) : BaseRequest()

data class PushFavoriteRes(
    val result: Result? = null
) : BaseResponse() {
    data class Result(val msg: String)
}