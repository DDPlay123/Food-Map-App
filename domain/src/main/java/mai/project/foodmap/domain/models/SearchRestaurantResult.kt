package mai.project.foodmap.domain.models

data class SearchRestaurantResult(
    val placeCount: Int,
    val placeId: String,
    val name: String,
    val address: String,
    val description: String,
    val isSearch: Boolean
)