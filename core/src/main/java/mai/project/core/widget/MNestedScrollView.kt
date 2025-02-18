package mai.project.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * 能夠嵌套 ViewPager2 的 NestedScrollView
 *
 * - 避免垂直與水平的滑動衝突
 */
class MNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var xDistance = 0f
    private var yDistance = 0f
    private var xLast = 0f
    private var yLast = 0f

    override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 重置距離累計，並記錄初始位置
                xDistance = 0f
                yDistance = 0f
                xLast = event.x
                yLast = event.y

                // 檢查子 view 是否包含 ViewPager2
                if (this.children.any { it is ViewPager2 }) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val curX = event.x
                val curY = event.y
                // 正確計算兩軸上的位移增量
                xDistance += abs(curX - xLast)
                yDistance += abs(curY - yLast)
                xLast = curX
                yLast = curY

                // 當水平移動多於垂直移動時，不攔截（讓子 view 處理）
                if (xDistance > yDistance) return false
            }
        }
        return super.onInterceptHoverEvent(event)
    }
}
