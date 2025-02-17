package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class GetLocationByAddressReq(
    override val accessKey: String,
    override val userId: String,
    val address: String
) : BaseRequest()

@Serializable
internal data class GetLocationByAddressRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val place: AutoCompleteModel
    )
}