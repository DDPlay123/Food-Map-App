package mai.project.foodmap.features.dialogs_features.prompt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PromptCallback : Parcelable {
    /**
     * 確認按鈕點擊事件
     *
     * @param outputString 輸出字串 (如果有開啟輸入框)
     */
    @Parcelize
    data class OnConfirm(
        val outputString: String
    ) : PromptCallback()

    /**
     * 取消按鈕點擊事件
     */
    @Parcelize
    data object OnCancel : PromptCallback()

    /**
     * Dialog 關閉事件
     */
    @Parcelize
    data object OnDismiss : PromptCallback()

    companion object {
        const val ARG_CONFIRM = "ARG_CONFIRM"
        const val ARG_CANCEL = "ARG_CANCEL"
        const val ARG_DISMISS = "ARG_DISMISS"
    }
}
