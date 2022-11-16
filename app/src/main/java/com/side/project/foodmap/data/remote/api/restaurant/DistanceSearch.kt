package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.*

data class DistanceSearchReq(
    override val accessKey: String,
    override val userId: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int = 100,
    val minNum: Int = 20,
    val maxNum: Int = 100
) : BaseRequest()

class DistanceSearchRes(
    val result: ArrayList<Result>? = null
) : BaseResponse() {
    class Result(
        val _id: String,
        val uid: String,
        val address: String,
        val icon: Icon,
        val location: Location,
        val name: String,
        val photos: ArrayList<Photos>,
        val rating: Rating,
        val status: String,
        val types: ArrayList<String>,
        val updateTime: String
    )
}

