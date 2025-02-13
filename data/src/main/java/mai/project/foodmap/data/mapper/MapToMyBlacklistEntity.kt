package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.localDataSource.entities.MyBlacklistEntity
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<List<RestaurantResult>>.mapToMyBlacklistEntities(): List<MyBlacklistEntity> {
    val copyData = data
    return copyData?.map {
        MyBlacklistEntity(index = it.placeId, result = it)
    } ?: emptyList()
}