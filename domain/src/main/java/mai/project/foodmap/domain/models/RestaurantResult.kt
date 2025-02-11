package mai.project.foodmap.domain.models

data class RestaurantResult(
    val placeCount: Int,
    val placeId: String,
    val name: String,
    val photos: List<String>,
    val ratingStar: Float,
    val ratingTotal: Long,
    val address: String,
    val lat: Double,
    val lng: Double,
    val distance: Double,
    val isFavorite: Boolean
)
