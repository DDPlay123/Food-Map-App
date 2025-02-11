package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class PushFavoriteListReq(
    override val accessKey: String,
    override val userId: String,
    val favoriteList: List<String>
) : BaseRequest()

@Serializable
internal data class PushFavoriteListRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}