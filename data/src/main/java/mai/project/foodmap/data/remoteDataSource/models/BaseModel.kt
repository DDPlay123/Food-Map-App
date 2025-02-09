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

@Serializable
internal data class LocationModel(
    val lat: Double,
    val lng: Double
)

@Serializable
internal data class RatingModel(
    val star: Float,
    val total: Long
)

@Serializable
internal data class IconModel(
    val url: String,
    @SerialName("background_color")
    val backgroundColor: String,
    @SerialName("mask_base_uri")
    val maskBaseUri: String
)

@Serializable
internal data class OpeningHoursModel(
    @SerialName("open_now")
    val openNow: Boolean,
    @SerialName("periods")
    var periods: List<Periods> = emptyList(),
    @SerialName("weekday_text")
    var weekdayText: List<String> = emptyList()
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
    val icon: IconModel,
    val types: List<String>,
    @SerialName("opening_hours")
    val openingHours: OpeningHoursModel,
    val distance: Double,
    val isFavorite: Boolean
)