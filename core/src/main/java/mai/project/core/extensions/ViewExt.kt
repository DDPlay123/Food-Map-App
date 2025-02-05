package mai.project.core.extensions

import android.view.View
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