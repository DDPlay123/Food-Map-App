package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable
import mai.project.foodmap.data.annotations.DrawCardMode

@Serializable
internal data class DrawCardReq(
    override val accessKey: String,
    override val userId: String,
    val location: LocationModel,
    @DrawCardMode
    val mode: Int,
    val num: Int
) : BaseRequest()

@Serializable
internal data class DrawCardRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String? = null,
        val updated: Boolean,
        val placeCount: Int,
        val placeList: List<PlaceListModel>
    )
}