package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.BuildConfig
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceDetailRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.RestaurantDetailResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<GetPlaceDetailRes>.mapToRestaurantDetailResult(
    userId: String
): NetworkResult<RestaurantDetailResult> {
    return mapResult {
        data?.result?.place?.let {
            RestaurantDetailResult(
                placeId = it.placeId,
                name = it.name,
                address = it.address,
                vicinity = it.vicinity,
                photos = it.photos?.map { photoId ->
                    "${BuildConfig.BASE_URL}api/place/get_html_photo/$photoId?userId=$userId"
                } ?: emptyList(),
                openNow = it.openingHours?.openNow,
                workDay = it.openingHours?.weekdayText ?: emptyList(),
                dineIn = it.dineIn,
                takeout = it.takeout,
                delivery = it.delivery,
                website = it.website,
                phone = it.phone,
                ratingStar = it.rating ?: 0f,
                ratingTotal = it.ratingsTotal,
                priceLevel = it.priceLevel ?: 0,
                lat = it.location.lat,
                lng = it.location.lng,
                shareLink = it.url.orEmpty(),
                reviews = it.reviews?.map { review ->
                    RestaurantDetailResult.Review(
                        authorName = review.authorName,
                        authorUrl = review.authorUrl,
                        profilePhotoUrl = review.profilePhotoUrl,
                        rating = review.rating,
                        text = review.text,
                        time = review.time
                    )
                } ?: emptyList(),
                isFavorite = data?.result?.isFavorite ?: false,
                isBlackList = data?.result?.isBlackList ?: false
            )
        }
    }
}