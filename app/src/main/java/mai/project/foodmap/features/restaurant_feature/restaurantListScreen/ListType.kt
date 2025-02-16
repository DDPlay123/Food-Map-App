package mai.project.foodmap.features.restaurant_feature.restaurantListScreen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ListType : Parcelable {

    @Parcelize
    data class KeywordSearch(
        val keyword: String
    ): ListType()

    @Parcelize
    data class DistanceSearch(
        val lat: Double,
        val lng: Double
    ): ListType()

    @Parcelize
    data object BlackList : ListType()
}