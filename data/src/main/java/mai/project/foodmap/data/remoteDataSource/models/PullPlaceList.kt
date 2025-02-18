package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PullPlaceListReq(
    override val accessKey: String,
    override val userId: String,
    @SerialName("place_id")
    val placeId: String
) : BaseRequest()

@Serializable
internal data class PullPlaceListRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}