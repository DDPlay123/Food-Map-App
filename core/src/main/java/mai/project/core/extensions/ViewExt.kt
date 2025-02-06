package mai.project.core.extensions

import android.view.View
import com.google.android.material.snackbar.Snackbar
import mai.project.core.utils.SafeOnClickListener

/**
 * 設定 View 的點擊事件
 *
 * @param safe 是否安全點擊
 * @param action 點擊後要做的事情
 */
fun View.onClick(
    safe: Boolean = true,
    action: (View) -> Unit
) = setOnClickListener(SafeOnClickListener(safe, action))

/**
 * 設定 View 的長按事件
 *
 * @param action 長按後要做的事情
 */
fun View.onLongClick(
    action: (View) -> Unit
) = setOnLongClickListener {
    action(it)
    true
}

/**
 * 設定多個 View 的點擊事件
 *
 * 如果要設定多個 View 的點擊事件，可以使用此方法
 *
 * @param safe 是否安全點擊
 * @param action 點擊後要做的事情
 */
fun List<View>.onClicks(
    safe: Boolean = true,
    action: (View) -> Unit
) {
    val clickListener = if (safe) {
        SafeOnClickListener(true, action)
    } else {
        View.OnClickListener(action)
    }

    this.forEach { view -> view.setOnClickListener(clickListener) }
}

/**
 * 清除多個 View 的點擊事件
 */
fun List<View>.clearClicks() {
    this.forEach { view -> view.setOnClickListener(null) }
}

/**
 * 設定多個 View 的長按事件
 *
 * 如果要設定多個 View 的長按事件，可以使用此方法
 *
 * @param action 長按後要做的事情
 */
fun List<View>.onLongClicks(
    action: (View) -> Unit
) {
    this.forEach { view -> view.onLongClick(action) }
}

/**
 * 清除多個 View 的長按事件
 */
fun List<View>.clearLongClicks() {
    this.forEach { view -> view.setOnLongClickListener(null) }
}

/**
 * 顯示 SnackBar
 *
 * @param message 訊息
 * @param actionText 按鈕文字 (不顯示則為空字串)
 * @param duration 顯示時間
 * @param anchorView 顯示位置 (在[anchorView]之上)
 * @param doSomething 點擊按鈕後要做的事情
 */
fun View.showSnackBar(
    message: String,
    actionText: String = "",
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
    setTextMaxLines(5)
    this.anchorView = anchorView
    animationMode = Snackbar.ANIMATION_MODE_FADE
    show()
}