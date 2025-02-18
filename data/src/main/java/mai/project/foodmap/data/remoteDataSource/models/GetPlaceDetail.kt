package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GetPlaceDetailReq(
    override val accessKey: String,
    override val userId: String,
    @SerialName("place_id")
    val placeId: String
) : BaseRequest()

@Serializable
internal data class GetPlaceDetailRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val isFavorite: Boolean,
        val isBlackList: Boolean,
        val place: PlaceModel
    )
}