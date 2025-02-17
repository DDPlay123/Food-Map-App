package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mai.project.foodmap.data.annotations.StatusCode

/**
 * 基底 Request
 */
internal abstract class BaseRequest {
    abstract val accessKey: String
    abstract val userId: String
}

/**
 * 基底 Response
 */
internal abstract class BaseResponse {
    @StatusCode
    abstract val status: Int
    abstract val errMsg: String?
}

/**
 * 經緯度座標
 */
@Serializable
internal data class LocationModel(
    val lat: Double,
    val lng: Double
)

/**
 * 評價資訊
 */
@Serializable
internal data class RatingModel(
    val star: Float? = null,
    val total: Long? = null
)

/**
 * 營業時段
 */
@Serializable
internal data class OpeningHoursModel(
    @SerialName("open_now")
    val openNow: Boolean? = null,
    @SerialName("weekday_text")
    val weekdayText: List<String>? = emptyList()
)

/**
 * 餐廳列表 Item
 */
@Serializable
internal data class PlaceListModel(
    @SerialName("place_id")
    val placeId: String,
    val updateTime: String,
    val status: String,
    val name: String,
    val photos: List<String>? = emptyList(),
    val rating: RatingModel,
    val address: String,
    val location: LocationModel,
    val types: List<String>,
    @SerialName("opening_hours")
    val openingHours: OpeningHoursModel? = null,
    val distance: Double,
    val isFavorite: Boolean
)

/**
 * 餐廳詳細資訊
 */
@Serializable
internal data class PlaceModel(
    @SerialName("place_id")
    val placeId: String,
    val photos: List<String>? = emptyList(),
    val name: String,
    val address: String,
    val vicinity: String,
    @SerialName("opening_hours")
    val openingHours: OpeningHoursModel? = null,
    @SerialName("dine_in")
    val dineIn: Boolean? = null,
    val takeout: Boolean? = null,
    val delivery: Boolean? = null,
    val website: String? = null,
    val phone: String? = null,
    val rating: Float? = null,
    @SerialName("ratings_total")
    val ratingsTotal: Long? = null,
    val reviews: List<Review>? = emptyList(),
    @SerialName("price_level")
    val priceLevel: Int? = null,
    val location: LocationModel,
    val url: String? = null
) {
    @Serializable
    data class Review(
        @SerialName("author_name")
        val authorName: String,
        @SerialName("author_url")
        val authorUrl: String,
        @SerialName("profile_photo_url")
        val profilePhotoUrl: String,
        val rating: Int,
        @SerialName("relative_time_description")
        val relativeTimeDescription: String,
        val text: String,
        val time: Int
    )
}

/**
 * Place 自動填充資訊
 */
@Serializable
internal data class AutoCompleteModel(
    @SerialName("place_id")
    val placeId: String,
    val name: String,
    val address: String,
    val description: String,
    val location: LocationModel? = null
)