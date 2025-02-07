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
 */
fun AppCompatActivity.launchAndRepeatStarted(
    vararg launchBlock: suspend () -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launchBlock.forEach {
                launch { it.invoke() }
            }
        }
    }
}

/**
 * 建立 LifecycleScope，不受 Activity/Fragment 的生命週期控制
 *
 * @param launchBlock 要執行的區塊
 */
fun AppCompatActivity.launchWithoutRepeat(
    vararg launchBlock: suspend () -> Unit
) {
    lifecycleScope.launch {
        launchBlock.forEach {
            launch { it.invoke() }
        }
    }
}