package mai.project.foodmap

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.domain.repository.PreferenceRepo
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoaderUtil: ImageLoaderUtil

    @Inject
    lateinit var preferenceRepo: PreferenceRepo

    override fun onCreate() {
        super.onCreate()
        // 設定顯示模式 (避免進入 Activity 後再切換，這樣會造成 UI 閃爍)
        runBlocking { setupNightMode() }
        // 設定 Debug 模式
        setupDebugMode()
        // 初始化 Firebase
        FirebaseApp.initializeApp(this)
    }

    override fun newImageLoader(): ImageLoader = imageLoaderUtil.imageLoader

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