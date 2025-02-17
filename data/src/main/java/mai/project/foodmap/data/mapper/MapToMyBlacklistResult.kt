package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.BuildConfig
import mai.project.foodmap.data.remoteDataSource.models.GetBlacklistRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.MyBlacklistResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<GetBlacklistRes>.mapToMyBlacklistResult(
    userId: String
): NetworkResult<List<MyBlacklistResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            MyBlacklistResult(
                placeCount = data.result.placeCount,
                placeId = it.placeId,
                name = it.name,
                photos = it.photos?.map { photoId ->
                    "${BuildConfig.BASE_URL}api/place/get_html_photo/$photoId?userId=$userId"
                } ?: emptyList(),
                ratingStar = it.rating.star ?: 0f,
                ratingTotal = it.rating.total ?: 0,
                address = it.address,
                lat = it.location.lat,
                lng = it.location.lng
            )
        }
    }
}