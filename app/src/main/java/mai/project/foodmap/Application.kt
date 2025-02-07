package mai.project.foodmap

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import mai.project.core.utils.ImageLoaderUtil
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoaderUtil: ImageLoaderUtil

    override fun onCreate() {
        super.onCreate()
        // 設定 Debug 模式
        setupDebugMode()
        // 初始化 Firebase
        FirebaseApp.initializeApp(this)
    }

    override fun newImageLoader(): ImageLoader = imageLoaderUtil.imageLoader

    /**
     * 設定 Debug 模式
     */
    private fun setupDebugMode() {
        if (BuildConfig.DEBUG) {
            // 設定 Timber
            Timber.plant(tagTree)
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