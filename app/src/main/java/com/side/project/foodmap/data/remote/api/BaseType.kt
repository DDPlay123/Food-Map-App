package com.side.project.foodmap.data.remote.api

import java.net.URL

/**
 * 本地資料
 */
class LocalData(val response: Any, val others: Any? = null)

/**
 * 基本 Request & Response
 */
abstract class BaseRequest {
    val accessKey: String? = null
    val userId: String? = null
}

abstract class BaseResponse {
    val requestTime: String? = null
    val status: Int = -1
    val errMsg: String? = null
    val verify: Boolean? = null
}

/**
 * Others
 */
data class Icon(
    val url: URL,
    val background_color: String,
    val mask_base_uri: URL
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Photos(
    val height: Long,
    val width: Long,
    val photo_reference: String
)

data class Rating(
    val star: Float,
    val total: Long
)