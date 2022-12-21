package com.side.project.foodmap.data.remote.api.user

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse
import com.side.project.foodmap.data.remote.api.FavoriteList

data class GetFavoriteReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

data class GetFavoriteRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val placeCount: Int,
        val placeList: ArrayList<FavoriteList>
    )
}