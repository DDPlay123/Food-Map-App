package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class GetBlockListReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

@Serializable
internal data class GetBlockListRes(
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