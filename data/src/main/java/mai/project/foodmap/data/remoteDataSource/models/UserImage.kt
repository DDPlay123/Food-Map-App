package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class SetUserImageReq(
    override val accessKey: String,
    override val userId: String,
    val userImage: String
) : BaseRequest()

@Serializable
internal data class SetUserImageRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val msg: String
    )
}

@Serializable
internal data class GetUserImageReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

@Serializable
internal data class GetUserImageRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val userImage: String
    )
}