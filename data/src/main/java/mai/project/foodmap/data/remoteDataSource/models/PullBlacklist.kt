package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class PullBlacklistReq(
    override val accessKey: String,
    override val userId: String,
    val placeIdList: List<String>
) : BaseRequest()

@Serializable
internal data class PullBlacklistRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}