package mai.project.foodmap.domain.models

data class SearchPlaceResult(
    val placeId: String,
    val name: String,
    val address: String,
    val description: String,
    val lat: Double? = null,
    val lng: Double? = null
)