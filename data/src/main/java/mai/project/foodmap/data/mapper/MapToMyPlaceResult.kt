package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceListRes
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<GetPlaceListRes>.mapToMyPlaceResults(): NetworkResult<List<MyPlaceResult>> {
    val copyData = data
    val result = copyData?.result?.placeList?.map {
        MyPlaceResult(
            status = copyData.status,
            placeCount = copyData.result.placeCount,
            placeId = it.placeId,
            name = it.name,
            address = it.address,
            lat = it.location.lat,
            lng = it.location.lng
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

internal fun NetworkResult<List<MyPlaceResult>>.mapToMySavedPlaceEntities(): List<MySavedPlaceEntity> {
    val copyData = data
    return copyData?.map {
        MySavedPlaceEntity(index = it.placeId, result = it)
    } ?: emptyList()
}