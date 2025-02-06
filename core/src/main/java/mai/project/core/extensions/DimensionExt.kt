package mai.project.core.extensions

import android.content.res.Resources
import android.util.TypedValue

/**
 * 將 Int 轉換為 DP
 */
val Int.DP
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

/**
 * 將 Float 轉換為 DP
 */
val Float.DP
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * 將 Int 轉換為 SP
 */
val Int.SP
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        toFloat(),
        Resources.getSystem().displayMetrics
    )

/**
 * 將 Float 轉換為 SP
 */
val Float.SP
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * 將 Pixel 轉換為 DP
 */
val Int.PxToDP
    get() = this / Resources.getSystem().displayMetrics.density

/**
 * 將 Pixel 轉換為 DP
 */
val Float.PxToDP
    get() = this / Resources.getSystem().displayMetrics.density

/**
 * 將 Pixel 轉換為 SP
 */
val Int.PxToSP: Float
    get() {
        val metrics = Resources.getSystem().displayMetrics
        return this / (metrics.density * Resources.getSystem().configuration.fontScale)
    }

/**
 * 將 Pixel 轉換為 SP
 */
val Float.PxToSP: Float
    get() {
        val metrics = Resources.getSystem().displayMetrics
        return this / (metrics.density * Resources.getSystem().configuration.fontScale)
    }
