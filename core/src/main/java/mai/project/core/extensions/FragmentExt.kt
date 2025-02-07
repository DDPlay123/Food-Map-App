package mai.project.core.extensions

import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * 建立 LifecycleScope 並在 Fragment 的生命週期為 STARTED 時重複執行
 *
 * @param launchBlock 要執行的區塊
 */
fun Fragment.launchAndRepeatStarted(
    vararg launchBlock: suspend () -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launchBlock.forEach {
                launch { it.invoke() }
            }
        }
    }
}

/**
 * 取得顏色資源
 *
 * @param color 顏色資源
 */
fun Fragment.getColorCompat(
    @ColorRes color: Int,
) = requireContext().getColorCompat(color)

/**
 * 取得顏色資源狀態
 *
 * @param color 顏色資源
 */
fun Fragment.getColorStateListCompat(
    @ColorRes color: Int,
) = requireContext().getColorStateListCompat(color)

/**
 * 取得 Drawable 資源
 *
 * @param drawable Drawable 資源
 */
fun Fragment.getDrawableCompat(
    @DrawableRes drawable: Int,
) = requireContext().getDrawableCompat(drawable)

/**
 * 顯示 Toast
 *
 * @param message 訊息
 * @param duration 顯示時間
 */
fun Fragment.displayToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
) = requireContext().displayToast(message, duration)