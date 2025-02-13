package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.localDataSource.entities.MyFavoriteEntity
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.domain.models.RestaurantDetailResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<List<MyFavoriteResult>>.mapToMyFavoriteEntities(): List<MyFavoriteEntity> {
    val copyData = data
    return copyData?.map {
        MyFavoriteEntity(index = it.placeId, result = it)
    } ?: emptyList()
}

internal fun RestaurantResult.mapToMyFavoriteEntity(): MyFavoriteEntity {
    return MyFavoriteEntity(
        index = placeId,
        result = MyFavoriteResult(
            placeId = placeId,
            photos = photos,
            name = name,
            address = address,
            workDay = emptyList(),
            dineIn = false,
            takeout = false,
            delivery = false,
            website = "",
            phone = "",
            ratingStar = ratingStar,
            ratingTotal = ratingTotal,
            priceLevel = 0,
            lat = lat,
            lng = lng,
            shareLink = ""
        )
    )
}

internal fun RestaurantDetailResult.mapToMyFavoriteEntity(): MyFavoriteEntity {
    return MyFavoriteEntity(
        index = placeId,
        result = MyFavoriteResult(
            placeId = placeId,
            photos = photos,
            name = name,
            address = address,
            workDay = workDay,
            dineIn = dineIn ?: false,
            takeout = takeout ?: false,
            delivery = delivery ?: false,
            website = website.orEmpty(),
            phone = phone.orEmpty(),
            ratingStar = ratingStar,
            ratingTotal = ratingTotal,
            priceLevel = priceLevel,
            lat = lat,
            lng = lng,
            shareLink = shareLink
        )
    )
}