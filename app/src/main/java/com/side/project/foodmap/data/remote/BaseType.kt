package com.side.project.foodmap.data.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.side.project.foodmap.util.Constants.GET_BLACK_LIST_MODEL
import com.side.project.foodmap.util.Constants.GET_FAVORITE_MODEL
import com.side.project.foodmap.util.Constants.GET_PLACE_LIST_MODEL
import com.side.project.foodmap.util.Constants.HISTORY_SEARCH_MODEL
import java.net.URL

/**
 * 基本 Request & Response
 */
abstract class BaseRequest {
    abstract val accessKey: String
    abstract val userId: String
}

abstract class BaseResponse {
    var requestTime: String = ""
    var status: Int = -1 // -1:未知錯誤、0:成功響應、1:帳號密碼格式錯誤、2:帳號已註冊、3:帳號不存在、4:accessKey錯誤
    var errMsg: String? = null
    var verify: Boolean = false
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

data class CloseOpen(
    val day: Int,
    val time: String
)

data class Periods(
    val close: CloseOpen,
    val open: CloseOpen
)

data class OpeningHours(
    val open_now: Boolean,
    var periods: List<Periods> = emptyList(),
    var weekday_text: List<String> = emptyList()
)

data class Info(
    var successCount: Int = -1,
    var placeNotFoundCount: Int = -1,
    var favoriteExistCount: Int = -1,
    var blackListExistCount: Int = -1,
    var updateCount: Int = -1,
    var deleteCount: Int = -1
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
    var photos: List<String> = emptyList(),
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

@Entity(tableName = GET_BLACK_LIST_MODEL)
data class PlaceList(
    @PrimaryKey
    val place_id: String,
    val updateTime: String,
    val status: String,
    val name: String,
    var photos: List<String> = emptyList(),
    val rating: Rating,
    val address: String,
    val location: Location,
    val icon: Icon,
    val types: List<String>,
    val opening_hours: OpeningHours,
    val distance: Double,
    var isFavorite: Boolean
)

@Entity(tableName = GET_FAVORITE_MODEL)
data class FavoriteList(
    @PrimaryKey
    val place_id: String,
    var photos: List<String> = emptyList(),
    val name: String,
    val vicinity: String,
    var workDay: List<String> = emptyList(),
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

@Entity(tableName = GET_PLACE_LIST_MODEL)
data class MyPlaceList(
    @PrimaryKey
    val place_id: String,
    val name: String,
    val address: String,
    val location: Location
)

@Entity(tableName = HISTORY_SEARCH_MODEL)
data class AutoComplete(
    val place_id: String,
    @PrimaryKey
    val name: String,
    val address: String,
    val description: String,
    var location: Location = Location(0.0, 0.0),
    var isSearch: Boolean = true
)