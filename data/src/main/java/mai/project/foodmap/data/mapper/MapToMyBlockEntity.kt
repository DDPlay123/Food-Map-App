package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.localDataSource.entities.MyBlockEntity
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<List<RestaurantResult>>.mapToMyBlockEntities(): List<MyBlockEntity> {
    val copyData = data
    return copyData?.map {
        MyBlockEntity(index = it.placeId, result = it)
    } ?: emptyList()
}