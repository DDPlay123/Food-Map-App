package mai.project.core.extensions

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import timber.log.Timber

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

/**
 * 檢查多個權限是否已授予
 *
 * @param allPermissions 權限陣列
 * @param needAllPermissions 是否需要全部權限都已授予
 */
fun Fragment.checkMultiplePermissions(
    allPermissions: Array<String>,
    needAllPermissions: Boolean = true,
) : Boolean {
    return if (needAllPermissions) {
        allPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    } else {
        allPermissions.any {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

/**
 * 請求多個權限
 *
 * @param allPermissions 權限陣列
 * @param needAllPermissions 是否需要全部權限都已授予
 * @param onGranted 權限已授予
 * @param onDenied 權限拒絕
 */
fun Fragment.requestMultiplePermissions(
    allPermissions: Array<String>,
    needAllPermissions: Boolean = true,
    onGranted: (() -> Unit)? = null,
    onDenied: (() -> Unit)? = null
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Timber.d(message = "權限請求結果：$permissions")
        when {
            needAllPermissions && permissions.all { it.key in allPermissions && it.value } ->
                onGranted?.invoke()

            !needAllPermissions && permissions.any { it.value } ->
                onGranted?.invoke()

            else -> onDenied?.invoke()
        }
    }
}