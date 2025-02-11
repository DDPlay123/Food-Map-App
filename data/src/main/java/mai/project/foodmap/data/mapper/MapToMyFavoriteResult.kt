package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.BuildConfig
import mai.project.foodmap.data.localDataSource.entities.MyFavoriteEntity
import mai.project.foodmap.data.remoteDataSource.models.GetFavoriteListRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<GetFavoriteListRes>.mapToMyFavoriteResult(
    userId: String
): NetworkResult<List<MyFavoriteResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            MyFavoriteResult(
                placeId = it.placeId,
                photos = it.photos?.map { photoId ->
                    "${BuildConfig.BASE_URL}api/place/get_html_photo/$photoId?userId=$userId"
                } ?: emptyList(),
                name = it.name,
                address = it.vicinity,
                workDay = it.workDay ?: emptyList(),
                dineIn = it.dineIn,
                takeout = it.takeout,
                delivery = it.delivery,
                website = it.website,
                phone = it.phone,
                ratingStar = it.rating,
                ratingTotal = it.ratingsTotal,
                priceLevel = it.priceLevel,
                lat = it.location.lat,
                lng = it.location.lng,
                shareLink = it.url
            )
        }
    }
}

internal fun NetworkResult<List<MyFavoriteResult>>.mapToMyFavoriteEntities(): List<MyFavoriteEntity> {
    val copyData = data
    return copyData?.map {
        MyFavoriteEntity(index = it.placeId, result = it)
    } ?: emptyList()
}