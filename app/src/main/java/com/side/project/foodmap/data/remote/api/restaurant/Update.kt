package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.BaseResponse

/**
 * 這要錢 ಥ_ಥ
 */
data class UpdateReq(
    val accessKey: String = "mmslab",
    val latitude: Double,
    val longitude: Double,
    val radius: Long
)

data class UpdateRes(
    val result: Result? = null
) : BaseResponse() {
    class Result {
        val matchCount: Int = 0
        val upsertCount: Int = 0
    }
}