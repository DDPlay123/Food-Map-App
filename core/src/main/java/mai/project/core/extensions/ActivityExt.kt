package mai.project.core.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * 建立 LifecycleScope 並在 Activity 的生命週期為 STARTED 時重複執行
 *
 * @param launchBlock 要執行的區塊
 * @param doAfterLaunch 執行完 launchBlock 後要執行的區塊
 */
fun AppCompatActivity.launchAndRepeatStarted(
    vararg launchBlock: suspend () -> Unit,
    doAfterLaunch: (() -> Unit)? = null
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launchBlock.forEach {
                launch { it.invoke() }
            }
            doAfterLaunch?.invoke()
        }
    }
}