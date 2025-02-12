package mai.project.foodmap.domain.models

data class RestaurantRouteResult(
    val distanceMeters: Int,
    val duration: Int,
    val polyline: String
)
