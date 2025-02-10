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
    val star: Float,
    val total: Long
)

/**
 * 營業時段
 */
@Serializable
internal data class OpeningHoursModel(
    @SerialName("open_now")
    val openNow: Boolean? = null,
    @SerialName("periods")
    val periods: List<Periods>? = emptyList(),
    @SerialName("weekday_text")
    val weekdayText: List<String>? = emptyList()
) {
    @Serializable
    data class Periods(
        val close: CloseOpen,
        val open: CloseOpen
    )

    @Serializable
    data class CloseOpen(
        val day: Int,
        val time: String
    )
}

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