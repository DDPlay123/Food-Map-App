package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchByDistanceReq(
    override val accessKey: String,
    override val userId: String,
    val location: LocationModel,
    val distance: Int,
    val skip: Int,
    val limit: Int
) : BaseRequest()

@Serializable
internal data class SearchByDistanceRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val placeCount: Int,
        val placeList: List<PlaceListModel>
    )
}