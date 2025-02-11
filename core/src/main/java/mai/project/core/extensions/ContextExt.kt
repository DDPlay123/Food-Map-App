package mai.project.core.extensions

import android.app.KeyguardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * 是否為鎖屏模式
 */
val Context.isScreenLocked: Boolean
    get() {
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        return keyguardManager.isKeyguardLocked
    }

/**
 * 取得螢幕寬度
 */
val Context.screenWidth: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = getSystemService(WindowManager::class.java).currentWindowMetrics
        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        metrics.widthPixels
    }

/**
 * 取得螢幕高度
 */
val Context.screenHeight: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = getSystemService(WindowManager::class.java).currentWindowMetrics
        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.height() - insets.bottom - insets.top
    } else {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        metrics.heightPixels
    }

/**
 * 取得顏色資源
 *
 * @param color 顏色資源
 */
fun Context.getColorCompat(
    @ColorRes color: Int
) = ContextCompat.getColor(this, color)

/**
 * 取得顏色資源狀態
 *
 * @param color 顏色資源
 */
fun Context.getColorStateListCompat(
    @ColorRes color: Int
) = ContextCompat.getColorStateList(this, color)

/**
 * 取得 Drawable 資源
 *
 * @param drawable Drawable 資源
 */
fun Context.getDrawableCompat(
    @DrawableRes drawable: Int
): Drawable? = ContextCompat.getDrawable(this, drawable)

/**
 * 顯示 Toast
 *
 * @param message 訊息
 * @param duration 顯示時間
 */
fun Context.displayToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT
) = Toast.makeText(this, message, duration).show()