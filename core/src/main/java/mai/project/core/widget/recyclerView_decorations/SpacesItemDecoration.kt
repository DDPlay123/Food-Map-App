package mai.project.core.widget.recyclerView_decorations

import android.graphics.Rect
import android.view.View
import androidx.annotation.Dimension
import androidx.recyclerview.widget.RecyclerView
import mai.project.core.annotations.Direction

/**
 * RecyclerView Item 間距設定
 *
 * @property direction 方向 [Direction.HORIZONTAL] or [Direction.VERTICAL]
 * @property space 裡面的間距
 * @property sideSpace 頭尾間距
 * @property startSpace 開始間距
 * @property endSpace 結束間距
 */
class SpacesItemDecoration(
    @Direction
    private val direction: Int,
    @Dimension(unit = Dimension.DP)
    private val space: Int = 0,
    private val sideSpace: Int = 0,
    private val startSpace: Int = 0,
    private val endSpace: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        val size = parent.adapter?.itemCount ?: 0
        val startSpace = if (startSpace > 0) startSpace else sideSpace
        val endSpace = if (endSpace > 0) endSpace else sideSpace

        when (direction) {
            Direction.HORIZONTAL -> {
                outRect.left = if (position == 0) startSpace else space / 2
                outRect.right = if (position == size - 1) endSpace else space / 2
            }

            Direction.VERTICAL -> {
                outRect.top = if (position == 0) startSpace else space / 2
                outRect.bottom = if (position == size - 1) endSpace else space / 2
            }
        }
    }
}
