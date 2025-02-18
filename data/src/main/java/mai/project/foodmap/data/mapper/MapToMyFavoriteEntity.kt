package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.localDataSource.entities.MyFavoriteEntity
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<List<MyFavoriteResult>>.mapToMyFavoriteEntities(): List<MyFavoriteEntity> {
    val copyData = data
    return copyData?.map {
        MyFavoriteEntity(index = it.placeId, result = it)
    } ?: emptyList()
}