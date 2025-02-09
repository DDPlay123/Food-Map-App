package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.BuildConfig
import mai.project.foodmap.data.remoteDataSource.models.DrawCardRes
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<DrawCardRes>.mapToRestaurantResults(
    userId: String
): NetworkResult<List<RestaurantResult>> {
    val copyData = data
    val result = copyData?.result?.placeList?.map {
        RestaurantResult(
            status = copyData.status,
            placeCount = copyData.result.placeCount,
            placeId = it.placeId,
            name = it.name,
            photos = it.photos?.map { photoId ->
                "${BuildConfig.BASE_URL}api/place/get_html_photo/$photoId?userId=$userId"
            } ?: emptyList(),
            ratingStar = it.rating.star,
            ratingTotal = it.rating.total,
            address = it.address,
            lat = it.location.lat,
            lng = it.location.lng,
            distance = it.distance,
            isFavorite = it.isFavorite
        )
    }
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(result)
        is NetworkResult.Error -> NetworkResult.Error(message, result)
        is NetworkResult.AccessKeyIllegal -> NetworkResult.AccessKeyIllegal()
        is NetworkResult.Loading -> NetworkResult.Loading()
        is NetworkResult.Idle -> NetworkResult.Idle()
    }
}
