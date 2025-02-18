package mai.project.foodmap.domain.models

data class MyFavoriteResult(
    val placeId: String,
    val photos: List<String>,
    val name: String,
    val address: String,
    val workDay: List<String>,
    val dineIn: Boolean,
    val takeout: Boolean,
    val delivery: Boolean,
    val website: String,
    val phone: String,
    val ratingStar: Float,
    val ratingTotal: Long,
    val priceLevel: Int,
    val lat: Double,
    val lng: Double,
    val shareLink: String,
    val isFavorite: Boolean
)