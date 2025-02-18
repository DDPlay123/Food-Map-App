package mai.project.foodmap

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mai.project.core.utils.ImageLoaderUtil
import mai.project.core.utils.notification.NotificationType
import mai.project.core.utils.notification.NotificationUtil
import mai.project.foodmap.domain.repository.PreferenceRepo
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), ImageLoaderFactory {

    @Inject
    lateinit var preferenceRepo: PreferenceRepo

    @Inject
    lateinit var notificationUtil: NotificationUtil

    override fun onCreate() {
        super.onCreate()
        // 設定顯示模式 (避免進入 Activity 後再切換，這樣會造成 UI 閃爍)
        runBlocking { setupNightMode() }
        // 設定 Debug 模式
        setupDebugMode()
        // 設定通道
        setupNotificationChannel()
        // 初始化 Firebase
        FirebaseApp.initializeApp(this)
        // 初始化 ImageLoader
        ImageLoaderUtil.initializeImageLoader(this)
    }

    override fun newImageLoader(): ImageLoader = ImageLoaderUtil.imageLoader

    /**
     * 設定顯示模式
     */
    private suspend fun setupNightMode() {
        val nightMode = preferenceRepo.readThemeMode.first()
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * 設定 Debug 模式
     */
    private fun setupDebugMode() {
        if (BuildConfig.DEBUG) {
            // 設定 Timber
            Timber.plant(tagTree)
        }
    }

    /**
     * 建立通知通道
     */
    private fun setupNotificationChannel() = with(notificationUtil) {
        createIcon(R.drawable.img_icon)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listOf(
                NotificationType.DEFAULT,
                // If more notification types
            ).apply {
                val currentChannels = getNotificationChannels()
                // 移除不存在的通道
                currentChannels.filterNot { type -> this.any { it.channelId == type.id } }
                    .forEach { closeNotificationChannel(it.id) }
                // 建立新的通道
                forEach(::createNotificationChannel)
            }
        }
    }

    companion object {
        /**
         * Timber 的 TagTree
         */
        private val tagTree: Timber.Tree
            get() {
                return object : Timber.DebugTree() {
                    override fun createStackElementTag(element: StackTraceElement): String =
                        "[${element.fileName}:${element.lineNumber}:${element.methodName}]"

                    override fun log(priority: Int, message: String?, vararg args: Any?) {
                        var adjustedMessage = message
                        message?.let { msg ->
                            val maxLength = 1000 // 最大長度

                            // 超過最大長度，就截斷
                            if (msg.length > maxLength) {
                                adjustedMessage = msg.substring(0, maxLength) + "…"
                            }
                        }
                        super.log(priority, adjustedMessage, *args)
                    }
                }
            }
    }
}