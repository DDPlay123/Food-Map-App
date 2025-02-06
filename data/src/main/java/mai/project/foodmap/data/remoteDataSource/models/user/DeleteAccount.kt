package mai.project.foodmap.data.remoteDataSource.models.user

import kotlinx.serialization.Serializable
import mai.project.foodmap.data.remoteDataSource.models.BaseRequest
import mai.project.foodmap.data.remoteDataSource.models.BaseResponse

@Serializable
data class DeleteAccountReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

@Serializable
data class DeleteAccountRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}