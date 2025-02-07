package mai.project.foodmap.features.dialogs_features.selector

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SelectorCallback : Parcelable {

    /**
     * Item 點擊事件
     *
     * @param item 點擊的 Item
     */
    @Parcelize
    data class OnItemClick(
        val item: SelectorModel
    ) : SelectorCallback()

    /**
     * Dialog 關閉事件
     */
    @Parcelize
    data object OnDismiss : SelectorCallback()

    companion object {
        const val ARG_ITEM_CLICK = "ARG_ITEM_CLICK"
        const val ARG_DISMISS = "ARG_DISMISS"
    }
}