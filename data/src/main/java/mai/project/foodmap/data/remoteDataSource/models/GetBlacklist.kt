package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GetBlacklistReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

@Serializable
internal data class GetBlacklistRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val placeCount: Int,
        val placeList: List<PlaceList>
    )

    @Serializable
    data class PlaceList(
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
        val openingHours: OpeningHoursModel? = null
    )
}