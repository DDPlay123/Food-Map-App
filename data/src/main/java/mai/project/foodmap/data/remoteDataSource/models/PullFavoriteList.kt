package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class PullFavoriteListReq(
    override val accessKey: String,
    override val userId: String,
    val favoriteIdList: List<String>
) : BaseRequest()

@Serializable
internal data class PullFavoriteListRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}