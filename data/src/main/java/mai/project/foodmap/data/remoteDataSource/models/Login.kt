package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginReq(
    val username: String,
    val password: String,
    val deviceId: String
)

@Serializable
internal data class LoginRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String,
        val userId: String,
        val accessKey: String
    )
}