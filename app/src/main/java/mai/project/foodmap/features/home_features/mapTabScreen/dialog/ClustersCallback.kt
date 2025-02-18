package mai.project.foodmap.features.home_features.mapTabScreen.dialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import mai.project.foodmap.features.home_features.mapTabScreen.utils.RestaurantClusterItem

sealed class ClustersCallback : Parcelable {

    /**
     * Item 點擊事件
     */
    @Parcelize
    data class OnItemClick(
        val item: RestaurantClusterItem
    ) : ClustersCallback()

    companion object {
        const val ARG_ITEM_CLICK = "ARG_ITEM_CLICK"
    }
}