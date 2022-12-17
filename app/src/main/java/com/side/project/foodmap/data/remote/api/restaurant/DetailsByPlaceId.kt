package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.*

data class DetailsByPlaceIdReq(
    override val accessKey: String,
    override val userId: String,
    val place_id: String
) : BaseRequest()

data class DetailsByPlaceIdRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val updated: Boolean,
        val isFavorite: Boolean,
        val updateTime: String,
        val place: Place
    )
}