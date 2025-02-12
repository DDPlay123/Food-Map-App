package mai.project.foodmap.domain.models

data class RestaurantDetailResult(
    val placeId: String,
    val name: String,
    val address: String,
    val vicinity: String,
    val photos: List<String>,
    val openNow: Boolean? = null,
    val workDay: List<String>,
    val dineIn: Boolean? = null,
    val takeout: Boolean? = null,
    val delivery: Boolean? = null,
    val website: String? = null,
    val phone: String? = null,
    val ratingStar: Float,
    val ratingTotal: Long,
    val priceLevel: Int,
    val lat: Double,
    val lng: Double,
    val shareLink: String,
    val reviews: List<Review>,
    val isFavorite: Boolean,
    val isBlackList: Boolean
) {
    data class Review(
        val authorName: String,
        val authorUrl: String,
        val profilePhotoUrl: String,
        val rating: Int,
        val text: String,
        val time: Int,
    )
}