package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class PlaceAutocompleteReq(
    override val accessKey: String,
    override val userId: String,
    val location: LocationModel,
    val input: String
) : BaseRequest()

@Serializable
internal data class PlaceAutocompleteRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val placeCount: Int,
        val placeList: List<AutoComplete>
    )
}