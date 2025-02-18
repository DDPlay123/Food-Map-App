package mai.project.core.utils.notification

import android.app.NotificationManager
import mai.project.core.R

/**
 * 通知類型 (只有 Android 8.0 以上需要)
 *
 * @param channelId 通道 ID (可使通知不發送特定類型的通知)
 * @param channelNameRes 通道名稱資源 (顯示在系統設定的通知管理頁面)
 * @param importance 通知重要性 (只有 Android 8.0 以上需要)
 */
enum class NotificationType(
    val channelId: String,
    val channelNameRes: Int,
    val importance: Int
) {
    // 預設通知
    DEFAULT(
        channelId = "default",
        channelNameRes = R.string.notification_default,
        importance = NotificationManager.IMPORTANCE_HIGH
    )
}