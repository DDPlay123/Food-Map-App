package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.BuildConfig
import mai.project.foodmap.data.remoteDataSource.models.DrawCardRes
import mai.project.foodmap.data.remoteDataSource.models.GetBlockListRes
import mai.project.foodmap.data.remoteDataSource.models.SearchByDistanceRes
import mai.project.foodmap.data.remoteDataSource.models.SearchByKeywordRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<DrawCardRes>.mapToRestaurantResultsWithDrawCardRes(
    userId: String
): NetworkResult<List<RestaurantResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            RestaurantResult(
                placeCount = data.result.placeCount,
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
    }
}

internal fun NetworkResult<SearchByDistanceRes>.mapToRestaurantResultsWithSearchByDistanceRes(
    userId: String
): NetworkResult<List<RestaurantResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            RestaurantResult(
                placeCount = data.result.placeCount,
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
    }
}

internal fun NetworkResult<SearchByKeywordRes>.mapToRestaurantResultsWithSearchByKeywordRes(
    userId: String
): NetworkResult<List<RestaurantResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            RestaurantResult(
                placeCount = data.result.placeCount,
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
    }
}

internal fun NetworkResult<GetBlockListRes>.mapToRestaurantResultsWithGetBlockListRes(
    userId: String
): NetworkResult<List<RestaurantResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            RestaurantResult(
                placeCount = data.result.placeCount,
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
    }
}
