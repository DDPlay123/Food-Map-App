package mai.project.foodmap.service

import android.app.PendingIntent
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.utils.notification.NotificationType
import mai.project.core.utils.notification.NotificationUtil
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import timber.log.Timber
import javax.inject.Inject

/**
 * Firebase Messaging Service
 *
 * - 通知消息：當 App 在前景時，會直接顯示在畫面上，並且會觸發 onMessageReceived()。
 * 當 App 處於背景或關閉時，這種消息會被系統自動處理，並顯示為系統通知。
 *
 * 可參考：https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages?hl=zh-tw#androidnotification
 * ```
 * {
 *   "notification": {
 *     "title": "標題",
 *     "body": "消息內容"
 *   }
 * }
 * ```
 * - 資料消息：無論 App 處於前台還是後台，這類消息都會觸發 FirebaseMessagingService 中的 onMessageReceived 方法。
 * ```
 * {
 *   "data": {
 *     "key1": "value1",
 *     "key2": "value2"
 *     // 其他自定義數據
 *   }
 * }
 * ```
 * 注意：這兩種消息是可以同時存在的，
 * 需要通知時，使用 notification 字段，不需要通知時，使用 data 字段。
 */
@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    /**
     * 通知工具
     */
    @Inject
    lateinit var notificationUtil: NotificationUtil

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d(message = "FCM Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = getDefaultPendingIntent()

        if (message.data.isNotEmpty()) {
            Timber.d(message = "FCM Data: ${message.data}")
        }

        message.notification?.let {
            handleNotification(it, intent)
        }
    }

    /**
     * 取得預設的 PendingIntent
     */
    private fun getDefaultPendingIntent(): PendingIntent {
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            applicationContext, 0, resultIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )
    }

    /**
     * 處理通知消息
     *
     * @param notification 通知
     * @param pendingIntent 點擊事件
     */
    private fun handleNotification(
        notification: RemoteMessage.Notification,
        pendingIntent: PendingIntent
    ) {
        val type = NotificationType.entries
            .find { it.channelId == notification.channelId }
            ?: NotificationType.DEFAULT

        notificationUtil.sendNormalNotification(
            title = notification.title ?: getString(R.string.app_name),
            message = notification.body ?: "Not found",
            pendingIntent = pendingIntent,
            type = type,
            sendId = notificationUtil.lastSendId + 1
        )
    }
}