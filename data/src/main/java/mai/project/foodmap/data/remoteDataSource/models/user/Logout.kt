package mai.project.foodmap.data.remoteDataSource.models.user

import kotlinx.serialization.Serializable
import mai.project.foodmap.data.remoteDataSource.models.BaseRequest
import mai.project.foodmap.data.remoteDataSource.models.BaseResponse

@Serializable
internal data class LogoutReq(
    override val accessKey: String,
    override val userId: String,
    val deviceId: String
) : BaseRequest()

@Serializable
internal data class LogoutRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}