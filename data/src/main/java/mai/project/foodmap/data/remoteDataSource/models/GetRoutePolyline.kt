package mai.project.foodmap.data.remoteDataSource.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GetRoutePolylineReq(
    override val accessKey: String,
    override val userId: String,
    val destination: LocationInfo,
    val origin: LocationInfo
) : BaseRequest() {
    @Serializable
    internal data class LocationInfo(
        @SerialName("place_id")
        val placeId: String,
        val lat: Double,
        val lng: Double
    )
}

@Serializable
internal data class GetRoutePolylineRes(
    override val status: Int,
    override val errMsg: String? = null,
    val result: Result? = null
) : BaseResponse() {
    @Serializable
    data class Result(
        val distanceMeters: Int,
        val duration: Int,
        val polyline: String
    )
}