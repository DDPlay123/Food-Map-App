package com.side.project.foodmap.data.remote.api

import java.net.URL

/**
 * 基本 Request & Response
 */
abstract class BaseRequest {
    abstract val accessKey: String
    abstract val userId: String
}

abstract class BaseResponse {
    var requestTime: String? = null
    var status: Int = -1 // -1:未知錯誤、0:成功響應、1:帳號密碼格式錯誤、2:帳號已註冊、3:帳號不存在、4:accessKey錯誤
    var errMsg: String? = null
    var verify: Boolean? = null
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

data class PlaceList(
    val _id: String,
    val uid: String,
    val address: String,
    val icon: Icon,
    val location: Location,
    val name: String,
    val photos: ArrayList<Photos>? = null,
    val rating: Rating,
    val status: String,
    val types: ArrayList<String>,
    val updateTime: String
)

data class FavoriteList(
    val placeId: String,
    val photos: List<String>,
    val name: String,
    val vicinity: String,
    val workDay: List<String>,
    val dine_in: Boolean,
    val takeout: Boolean,
    val delivery: Boolean,
    val website: String,
    val phone: String,
    val rating: Float,
    val ratings_total: Long
)