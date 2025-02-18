package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class SetPasswordReq(
    override val accessKey: String,
    override val userId: String,
    val password: String
) : BaseRequest()

@Serializable
internal data class SetPasswordRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}