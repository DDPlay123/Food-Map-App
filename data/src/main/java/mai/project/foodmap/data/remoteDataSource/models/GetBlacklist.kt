package mai.project.foodmap.data.remoteDataSource.models

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
        val placeList: List<PlaceListModel>
    )
}