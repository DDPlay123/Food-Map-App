package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.remoteDataSource.models.GetRoutePolylineRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.RestaurantRouteResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<GetRoutePolylineRes>.mapToRestaurantRouteResult(): NetworkResult<RestaurantRouteResult> {
    return mapResult { data ->
        data?.result?.let {
            RestaurantRouteResult(
                distanceMeters = it.distanceMeters,
                duration = it.duration,
                polyline = it.polyline
            )
        }
    }
}