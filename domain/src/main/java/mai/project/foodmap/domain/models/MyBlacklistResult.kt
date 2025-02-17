package mai.project.foodmap.domain.models

data class MyBlacklistResult(
    val placeCount: Int,
    val placeId: String,
    val name: String,
    val photos: List<String>,
    val ratingStar: Float,
    val ratingTotal: Long,
    val address: String,
    val lat: Double,
    val lng: Double
)