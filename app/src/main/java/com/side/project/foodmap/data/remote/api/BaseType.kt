package com.side.project.foodmap.data.remote.api

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.side.project.foodmap.util.Constants.GET_FAVORITE_MODEL
import com.side.project.foodmap.util.Constants.HISTORY_SEARCH
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

data class OpeningHours(
    val open_now: Boolean,
    val weekday_text: List<String>? = null
)

data class Info(
    val successCount: Int? = null,
    val placeNotFoundCount: Int? = null,
    val favoriteExistCount: Int? = null,
    val blackListExistCount: Int? = null,
    val deleteCount: Int? = null
)

data class Review(
    val author_name: String,
    val author_url: String,
    val language: String,
    val original_language: String,
    val profile_photo_url: String,
    val rating: Int,
    val relative_time_description: String,
    val text: String,
    val time: Int,
    val translated: Boolean
)

data class Place(
    val opening_hours: OpeningHours,
    val address: String,
    val phone: String? = null,
    val location: Location,
    val name: String,
    val photos: ArrayList<String>? = null,
    val place_id: String,
    val rating: Float? = null,
    val reviews: List<Review>? = null,
    val delivery: Boolean? = null,
    val dine_in: Boolean? = null,
    val takeout: Boolean? = null,
    val price_level: Int? = null,
    val url: String? = null,
    val ratings_total: Long? = null,
    val vicinity: String,
    val website: String? = null
)

data class PlaceList(
    val place_id: String,
    val updateTime: String,
    val status: String,
    val name: String,
    val photos: List<String>? = null,
    val rating: Rating,
    val address: String,
    val location: Location,
    val icon: Icon,
    val types: List<String>,
    val opening_hours: OpeningHours,
    val distance: Double? = null,
    val isFavorite: Boolean? = null
)

@Entity(tableName = GET_FAVORITE_MODEL)
data class FavoriteList(
    @PrimaryKey
    val place_id: String,
    val photos: List<String>? = null,
    val name: String,
    val vicinity: String,
    val workDay: List<String>? = null,
    val dine_in: Boolean,
    val takeout: Boolean,
    val delivery: Boolean,
    val website: String,
    val phone: String,
    val rating: Float,
    val ratings_total: Long,
    val price_level: Int,
    val location: Location,
    val url: String
)

@Entity(tableName = HISTORY_SEARCH)
data class HistorySearch(
    @PrimaryKey
    val place_id: String,
    val name: String,
    val address: String
)