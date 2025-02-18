package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PushPlaceListReq(
    override val accessKey: String,
    override val userId: String,
    @SerialName("place_id")
    val placeId: String,
    val name: String,
    val address: String,
    val location: LocationModel
) : BaseRequest()

@Serializable
internal data class PushPlaceListRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}