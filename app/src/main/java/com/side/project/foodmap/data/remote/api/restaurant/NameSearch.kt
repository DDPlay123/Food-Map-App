package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.*

data class NameSearchReq(
    val name: String
) : BaseRequest()

class NameSearchRes(
    val result: Result? = null
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

