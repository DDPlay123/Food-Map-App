package com.side.project.foodmap.data.remote.geocode

import com.side.project.foodmap.data.remote.*

data class GetLocationByAddressReq(
    override val accessKey: String,
    override val userId: String,
    val address: String
) : BaseRequest()

data class GetLocationByAddressRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val updated: Boolean,
        val place: AutoComplete
    )
}