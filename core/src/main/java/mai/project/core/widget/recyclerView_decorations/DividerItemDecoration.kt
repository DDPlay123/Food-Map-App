package mai.project.core.widget.recyclerView_decorations

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import mai.project.core.annotations.Direction
import mai.project.core.extensions.getDrawableCompat

/**
 * RecyclerView 分隔線設定
 *
 * @param context Context
 * @param direction 方向 [Direction.HORIZONTAL] or [Direction.VERTICAL]
 * @param dividerHeight 分隔線高度
 * @param marginTop 上面間距
 * @param marginBottom 下面間距
 * @param marginLeft 左邊間距
 * @param marginRight 右邊間距
 * @param dividerDrawableRes 分隔線 Drawable
 */
class DividerItemDecoration(
    private val context: Context,
    @Direction
    private val direction: Int,
    private val dividerHeight: Int,
    @Dimension(unit = Dimension.DP)
    private val marginTop: Int = 0,
    @Dimension(unit = Dimension.DP)
    private val marginBottom: Int = 0,
    @Dimension(unit = Dimension.DP)
    private val marginLeft: Int = 0,
    @Dimension(unit = Dimension.DP)
    private val marginRight: Int = 0,
    @DrawableRes
    private val dividerDrawableRes: Int
) : RecyclerView.ItemDecoration() {

    private val divider: Drawable by lazy {
        context.getDrawableCompat(dividerDrawableRes)
            ?: throw IllegalArgumentException("Invalid drawable resource")
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (direction == Direction.VERTICAL) {
            drawVerticalDivider(canvas, parent)
        } else {
            drawHorizontalDivider(canvas, parent)
        }
    }

    private fun drawVerticalDivider(canvas: Canvas, parent: RecyclerView) {
        val dividerLeft = parent.paddingLeft + marginLeft
        val dividerRight = parent.width - parent.paddingRight - marginRight

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin + marginTop
            val bottom = top + dividerHeight

            divider.setBounds(dividerLeft, top, dividerRight, bottom)
            divider.draw(canvas)
        }
    }

    private fun drawHorizontalDivider(canvas: Canvas, parent: RecyclerView) {
        val dividerTop = parent.paddingTop + marginTop
        val dividerBottom = parent.height - parent.paddingBottom - marginBottom

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val left = child.right + params.rightMargin + marginLeft
            val right = left + dividerHeight

            divider.setBounds(left, dividerTop, right, dividerBottom)
            divider.draw(canvas)
        }
    }
}