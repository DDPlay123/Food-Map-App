package com.side.project.foodmap.data.remote.user.favoriteList

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse
import com.side.project.foodmap.data.remote.FavoriteList

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