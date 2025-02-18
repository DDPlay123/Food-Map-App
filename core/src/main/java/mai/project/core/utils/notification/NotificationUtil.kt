package mai.project.core.utils.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知工具
 *
 * @property isNotificationEnabled 檢測通知是否開啟
 * @property isNotificationChannelEnabled 檢測通知通道是否開啟
 * @property createIcon 建立 Icon 圖示
 * @property createNotificationChannel 建立通知通道
 * @property createNotificationBuilder 建立通知建造者
 * @property sendNormalNotification 發送一般的通知
 * @property sendProgressNotification 發送進度條通知
 * @property cancelSpecifyNotification 取消指定的通知
 */
@Singleton
class NotificationUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 最後發送的通知 ID
     */
    var lastSendId: Int = DEFAULT_SEND_ID
        private set

    /**
     * Small Icon 圖示
     */
    private var smallIcon: Int = 0

    /**
     * notificationManager [NotificationManager]
     */
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    /**
     * 檢測通知是否開啟
     */
    val isNotificationEnabled: Boolean
        get() = notificationManager.areNotificationsEnabled()

    /**
     * 檢測通知通道是否開啟
     */
    fun isNotificationChannelEnabled(type: NotificationType): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        return notificationManager.getNotificationChannel(type.channelId)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    /**
     * 建立 Icon 圖示
     *
     * @param iconRes 圖示資源
     */
    fun createIcon(@DrawableRes iconRes: Int) {
        smallIcon = iconRes
    }

    /**
     * 建立 Notification 通道 (只有 Android 8.0 以上需要)
     *
     * @param type [NotificationType] 通知類型
     */
    fun createNotificationChannel(type: NotificationType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        // 建立通道
        NotificationChannel(
            type.channelId, context.getString(type.channelNameRes), type.importance
        ).apply {
            enableLights(true)
            enableVibration(true)
            notificationManager.createNotificationChannel(this)
        }
    }

    /**
     * 刪除指定的通知通道
     *
     * @param channelId 通知通道 ID
     */
    fun closeNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        notificationManager.deleteNotificationChannel(channelId)
    }

    /**
     * 列出當前所有通知通道
     */
    fun getNotificationChannels(): List<NotificationChannel> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()
        return notificationManager.notificationChannels.toList()
    }

    /**
     * 建立 Notification.Builder
     *
     * @param type [NotificationType] 通知類型
     * @param title 標題
     * @param message 內容
     * @param autoCancel 點擊後是否自動取消
     * @param ongoing 設定是否為持續通知
     * @param pendingIntent 點擊後要執行的 Intent
     */
    fun createNotificationBuilder(
        type: NotificationType,
        title: String,
        message: String,
        autoCancel: Boolean = true,
        ongoing: Boolean = false,
        pendingIntent: PendingIntent? = null
    ): NotificationCompat.Builder {
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, type.channelId)
        } else {
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(context)
        }

        return notificationBuilder.apply {
            setContentTitle(title)
            setContentText(message)
            setSmallIcon(smallIcon)
            setColor(Color.parseColor("#F52C56"))
            setAutoCancel(autoCancel) // 點擊後是否自動取消
            setOngoing(ongoing) // 設定是否為持續通知
            pendingIntent?.let { setContentIntent(it) }
            // 設定通道ID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationBuilder.setChannelId(type.channelId)
            }
        }
    }

    /**
     * 發送一般的 Notification
     *
     * @param title 標題
     * @param message 內容
     * @param pendingIntent 點擊後要執行的 Intent
     * @param type 通知類型
     * @param sendId 這則通知的唯一 ID (與建立通道的 ID 不同)
     */
    fun sendNormalNotification(
        title: String,
        message: String,
        pendingIntent: PendingIntent? = null,
        type: NotificationType,
        sendId: Int = DEFAULT_SEND_ID
    ) {
        val notificationBuilder = createNotificationBuilder(
            type = type,
            title = title,
            message = message,
            pendingIntent = pendingIntent
        )

        lastSendId = sendId
        notificationManager.notify(lastSendId, notificationBuilder.build())
    }

    /**
     * 發送進度條 Notification
     *
     * @param title 標題
     * @param max 最大值
     * @param progress 目前進度
     * @param indeterminate 是否為不確定進度
     * @param type 通知類型
     * @param sendId 這則通知的唯一 ID (與建立通道的 ID 不同)
     */
    fun sendProgressNotification(
        title: String? = null,
        max: Int,
        progress: Int,
        indeterminate: Boolean = false,
        type: NotificationType,
        sendId: Int
    ) {
        // 判斷是否完成
        if (progress >= max) {
            cancelSpecifyNotification(sendId)
            return
        }

        val notificationBuilder = createNotificationBuilder(
            type = type,
            title = title ?: "Loading...",
            message = "Progress rate：$progress/$max",
            autoCancel = false
        )

        notificationBuilder
            .setProgress(max, progress, indeterminate)
            .setCategory(Notification.CATEGORY_SOCIAL)

        lastSendId = sendId
        notificationManager.notify(lastSendId, notificationBuilder.build())
    }

    /**
     * 取消指定的 Notification
     *
     * @param sendId 這則通知的唯一 ID (與建立通道的 ID 不同)
     */
    fun cancelSpecifyNotification(
        sendId: Int
    ) = notificationManager.cancel(sendId)

    companion object {
        /**
         * 預設的發送 ID
         */
        const val DEFAULT_SEND_ID = 0
    }
}