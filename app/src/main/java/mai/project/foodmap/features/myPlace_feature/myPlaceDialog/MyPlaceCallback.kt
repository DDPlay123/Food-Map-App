package mai.project.foodmap.features.myPlace_feature.myPlaceDialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class MyPlaceCallback : Parcelable {

    /**
     * Item 點擊事件
     *
     * @param placeId 點擊的定位點
     */
    @Parcelize
    data class OnItemClick(
        val placeId: String
    ) : MyPlaceCallback()

    /**
     * 新增定位點事件
     */
    @Parcelize
    data object OnAddAddress : MyPlaceCallback()

    companion object {
        const val ARG_ITEM_CLICK = "ARG_ITEM_CLICK"
        const val ARG_ADD_ADDRESS = "ARG_ADD_ADDRESS"
    }
}