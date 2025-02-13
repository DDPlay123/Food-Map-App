package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.remoteDataSource.models.GetPlaceListRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<GetPlaceListRes>.mapToMyPlaceResults(): NetworkResult<List<MyPlaceResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            MyPlaceResult(
                placeCount = data.result.placeCount,
                placeId = it.placeId,
                name = it.name,
                address = it.address,
                lat = it.location.lat,
                lng = it.location.lng
            )
        }
    }
}