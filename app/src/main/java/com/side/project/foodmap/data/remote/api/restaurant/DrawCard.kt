package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse
import com.side.project.foodmap.data.remote.api.PlaceList
import java.io.Serializable

data class DrawCardReq(
    override val accessKey: String,
    override val userId: String,
    val latitude: Double,
    val longitude: Double,
    val mode: Int, // 0：附近熱門餐廳，1：最愛中的熱門餐廳
    val num: Int = 10
) : BaseRequest()

class DrawCardRes(
    val result: Result
) : BaseResponse(), Serializable {
    data class Result(
        val updated: Boolean,
        val placeCount: Long,
        val placeList: ArrayList<PlaceList>
    )
}