package mai.project.foodmap.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.viewbinding.ViewBinding
import mai.project.foodmap.ui.MainActivity
import timber.log.Timber

/**
 * 基礎 Activity ，用於繼承
 */
abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    protected val binding by lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

    protected abstract val viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    companion object {
        /**
         * 是否重新啟動
         */
        const val KEY_IS_RESTART = "KEY_IS_RESTART"
    }
}