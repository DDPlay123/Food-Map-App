package mai.project.foodmap.base

import android.app.LocaleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import mai.project.foodmap.MainActivity
import mai.project.foodmap.data.annotations.LanguageMode
import timber.log.Timber
import java.util.Locale

/**
 * 基礎 Activity ，用於繼承
 *
 * example：
 * ```
 * // 無 ViewModel
 * class MainActivity : BaseActivity<ActivityMainBinding, Nothing>(
 *     bindingInflater = ActivityMainBinding::inflate
 * ) {
 *     ... // 不用覆寫 viewModel
 * }
 *
 * // 有 ViewModel
 * class MainActivity : BaseActivity<ActivityMainBinding, SharedViewModel>(
 *     bindingInflater = ActivityMainBinding::inflate
 * ) {
 *     override val viewModel by viewModels<SharedViewModel>()
 * }
 * ```
 */
abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    protected val binding by lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

    protected open val viewModel: VM? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 修正在切換顯示模式時，無法進入 EdgeToEdge 問題
        // reference：https://stackoverflow.com/a/79325812
        if (savedInstanceState != null && Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        enableEdgeToEdge()
        setContentView(binding.root)
        Timber.d(message = "${this::class.simpleName} onCreate")
    }

    override fun onStart() {
        super.onStart()
        Timber.d(message = "${this::class.simpleName} onStart")
    }

    override fun onResume() {
        super.onResume()
        Timber.d(message = "${this::class.simpleName} onResume")
    }

    override fun onPause() {
        super.onPause()
        Timber.d(message = "${this::class.simpleName} onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d(message = "${this::class.simpleName} onDestroy")
    }

    /**
     * 重新啟動 APP
     *
     * @param bundle 附加資料
     */
    protected fun restartApplication(
        bundle: Bundle? = null
    ) {
        Intent(applicationContext, MainActivity::class.java).apply {
            Timber.d(message = "重新啟動 APP")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtras(bundleOf(KEY_IS_RESTART to true))
            bundle?.let(::putExtras)
            startActivity(this)
            // 清空當前所有執行緒，(不要使用，我們在重啟時會清空資料，所以不需要清空執行緒。)
//            android.os.Process.killProcess(android.os.Process.myPid())
        }
        finish()
    }

    /**
     * 設定語言
     *
     * @param languageMode 語言模式
     */
    protected fun setAppLanguage(@LanguageMode languageMode: String) {
        if (Build.VERSION.SDK_INT >= 33) {
            // Android 13+ 使用 LocaleManager，無需重啟 Activity
            getSystemService(LocaleManager::class.java)?.applicationLocales =
                android.os.LocaleList.forLanguageTags(languageMode)
        } else {
            // Android 12 以下
            updateResourcesLegacy(languageMode)
            restartApplication()
        }
    }

    /**
     * 更新語言
     *
     * - Android 12 以下，需要手動更新並重啟系統
     */
    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(@LanguageMode languageMode: String) {
        val locale = if (languageMode == LanguageMode.SYSTEM) Locale.getDefault() else Locale.forLanguageTag(languageMode)
        Locale.setDefault(locale)
        resources.configuration.setLocale(locale)
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)
    }

    companion object {
        /**
         * 是否重新啟動
         */
        const val KEY_IS_RESTART = "KEY_IS_RESTART"
    }
}