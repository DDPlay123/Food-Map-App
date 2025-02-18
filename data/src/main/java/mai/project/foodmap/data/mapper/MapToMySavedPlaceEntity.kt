package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<List<MyPlaceResult>>.mapToMySavedPlaceEntities(): List<MySavedPlaceEntity> {
    val copyData = data
    return copyData?.map {
        MySavedPlaceEntity(index = it.placeId, result = it)
    } ?: emptyList()
}