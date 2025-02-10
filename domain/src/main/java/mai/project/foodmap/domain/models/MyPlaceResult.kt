package mai.project.foodmap.domain.models

data class MyPlaceResult(
    val status: Int,
    val placeCount: Int,
    val placeId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)