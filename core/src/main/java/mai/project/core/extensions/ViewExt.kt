package mai.project.core.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import mai.project.core.utils.SafeOnClickListener

/**
 * 設定 View 的點擊事件
 *
 * @param safe 是否安全點擊
 * @param anim 是否需要動畫效果
 * @param isAnimEndCallback 是否需要動畫結束後要做的事情
 * @param action 點擊後要做的事情
 */
fun View.onClick(
    safe: Boolean = true,
    anim: Boolean = false,
    isAnimEndCallback: Boolean = false,
    action: (View) -> Unit
) = setOnClickListener(SafeOnClickListener(safe, anim, isAnimEndCallback, action))

/**
 * 設定多個 View 的點擊事件
 *
 * 如果要設定多個 View 的點擊事件，可以使用此方法
 *
 * @param safe 是否安全點擊
 * @param anim 是否需要動畫效果
 * @param isAnimEndCallback 是否需要動畫結束後要做的事情
 * @param action 點擊後要做的事情
 */
fun List<View>.onClicks(
    safe: Boolean = true,
    anim: Boolean = false,
    isAnimEndCallback: Boolean = false,
    action: (View) -> Unit
) = this.forEach { view -> view.onClick(safe, anim, isAnimEndCallback, action) }

/**
 * 生成 Adapter 的 ViewBinding
 *
 * @param attachToParent 是否附加到父視圖
 */
fun <VB : ViewBinding> ViewGroup.inflateBinding(
    inflateMethod: (LayoutInflater, ViewGroup, Boolean) -> VB,
    attachToParent: Boolean = false
): VB {
    val layoutInflater = LayoutInflater.from(context)
    return inflateMethod(layoutInflater, this, attachToParent)
}

/**
 * 顯示 SnackBar
 *
 * @param message 訊息
 * @param actionText 按鈕文字 (不顯示則為空字串)
 * @param actionTextColor 按鈕文字顏色 (不設定則為預設顏色)
 * @param duration 顯示時間
 * @param anchorView 顯示位置 (在[anchorView]之上)
 * @param doSomething 點擊按鈕後要做的事情
 */
fun View.showSnackBar(
    message: String,
    actionText: String = "",
    @ColorInt
    actionTextColor: Int = -1,
    duration: Int = Snackbar.LENGTH_SHORT,
    anchorView: View? = null,
    doSomething: ((Snackbar) -> Unit)? = null
) = with(Snackbar.make(this, message, duration)) {
    if (actionText.isNotEmpty()) {
        setAction(actionText) {
            doSomething?.invoke(this)
            dismiss()
        }
    }
    if (actionTextColor != -1) {
        setActionTextColor(actionTextColor)
    }
    setTextMaxLines(5)
    this.anchorView = anchorView
    animationMode = Snackbar.ANIMATION_MODE_FADE
    show()
}