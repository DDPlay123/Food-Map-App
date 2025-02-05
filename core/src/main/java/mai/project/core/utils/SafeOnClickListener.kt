package mai.project.core.utils

import android.view.View

/**
 * 避免 View 的 setOnClickListener 被重複觸發，所以設定一個安全的間隔時間。
 *
 * @param isSafe 是否需要安全間隔時間
 * @param action 點擊事件
 */
class SafeOnClickListener(
    private val isSafe: Boolean,
    private val action: (view: View) -> Unit
) : View.OnClickListener {
    private var lastClickTimestamp = 0L

    override fun onClick(clickedView: View) {
        if (!isSafe) {
            action.invoke(clickedView)
            return
        }

        // 取得當前時間
        val currentTimestamp = System.currentTimeMillis()
        // 如果當前時間 - 上次點擊時間 > 安全間隔時間，則執行點擊事件
        if (lastClickTimestamp == 0L || currentTimestamp - lastClickTimestamp > SAFE_INTERVAL) {
            action.invoke(clickedView)
            lastClickTimestamp = currentTimestamp
        }
    }

    private companion object {
        private const val SAFE_INTERVAL = 650
    }
}