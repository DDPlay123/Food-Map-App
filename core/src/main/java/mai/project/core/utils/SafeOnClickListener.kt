package mai.project.core.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import mai.project.core.R

/**
 * 避免 View 的 setOnClickListener 被重複觸發，所以設定一個安全的間隔時間。
 *
 * @param isSafe 是否需要安全間隔時間
 * @param isAnim 是否需要動畫效果
 * @param isAnimEndCallback 是否需要動畫結束後要做的事情
 * @param action 點擊事件
 */
class SafeOnClickListener(
    private val isSafe: Boolean,
    private val isAnim: Boolean,
    private val isAnimEndCallback: Boolean,
    private val action: (view: View) -> Unit
) : View.OnClickListener {
    private var lastClickTimestamp = 0L

    override fun onClick(clickedView: View) {
        if (isAnim) {
            handleAnimClick(clickedView)
        } else {
            handleClick(clickedView)
        }
    }

    /**
     * 處理動畫效果點擊事件
     */
    private fun handleAnimClick(clickedView: View) {
        val animation = AnimationUtils.loadAnimation(clickedView.context, R.anim.small_to_large)
        clickedView.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                if (!isAnimEndCallback) handleClick(clickedView)
            }

            override fun onAnimationEnd(p0: Animation?) {
                if (isAnimEndCallback) handleClick(clickedView)
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
    }

    /**
     * 處理安全間隔時間點擊事件
     */
    private fun handleClick(clickedView: View) {
        if (!isSafe) {
            action.invoke(clickedView)
            return
        }

        // 取得當前時間
        val currentTimestamp = System.currentTimeMillis()
        // 如果當前時間 - 上次點擊時間 > 安全間隔時間，則執行點擊事件
        if (lastClickTimestamp == 0L || currentTimestamp - lastClickTimestamp > SAFE_INTERVAL) {
            action.invoke(clickedView)
            lastClickTimestamp = currentTimestamp
        }
    }

    private companion object {
        private const val SAFE_INTERVAL = 650
    }
}