package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SearchAutocompleteReq(
    override val accessKey: String,
    override val userId: String,
    val location: LocationModel,
    val distance: Long,
    val input: String
) : BaseRequest()

@Serializable
internal data class SearchAutocompleteRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val placeCount: Int,
        val placeList: List<AutoComplete>
    )

    @Serializable
    data class AutoComplete(
        @SerialName("place_id")
        val placeId: String,
        val name: String,
        val address: String,
        val description: String,
        val isSearch: Boolean
    )
}