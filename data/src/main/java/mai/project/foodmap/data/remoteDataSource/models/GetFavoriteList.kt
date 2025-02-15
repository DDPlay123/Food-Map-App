package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GetFavoriteListReq(
    override val accessKey: String,
    override val userId: String
) : BaseRequest()

@Serializable
internal data class GetFavoriteListRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val placeCount: Int,
        val placeList: List<PlaceList>
    )

    @Serializable
    data class PlaceList(
        @SerialName("place_id")
        val placeId: String,
        val photos: List<String>? = emptyList(),
        val name: String,
        val vicinity: String,
        val workDay: List<String>? = emptyList(),
        @SerialName("dine_in")
        val dineIn: Boolean,
        val takeout: Boolean,
        val delivery: Boolean,
        val website: String,
        val phone: String,
        val rating: Float,
        @SerialName("ratings_total")
        val ratingsTotal: Long,
        @SerialName("price_level")
        val priceLevel: Int,
        val location: LocationModel,
        val url: String
    )
}