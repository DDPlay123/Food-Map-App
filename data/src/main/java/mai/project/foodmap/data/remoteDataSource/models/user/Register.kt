package mai.project.foodmap.data.remoteDataSource.models.user

import kotlinx.serialization.Serializable
import mai.project.foodmap.data.remoteDataSource.models.BaseResponse

@Serializable
internal data class RegisterReq(
    val username: String,
    val password: String,
    val deviceId: String
)

@Serializable
internal data class RegisterRes(
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