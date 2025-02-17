package mai.project.core.widget.recyclerView_decorations

import android.graphics.Rect
import android.view.View
import androidx.annotation.Dimension
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView Item 間距設定
 *
 * 主要用於 GridLayoutManager
 *
 * @property spanCount 每行數量
 * @property space 間距
 */
class GridItemDecoration(
    private val spanCount: Int,
    @Dimension(unit = Dimension.DP)
    private val space: Int = 0,
    @Dimension(unit = Dimension.DP)
    private val sideSpace: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (column == 0) {
            outRect.left = sideSpace
        } else {
            outRect.left = space - column * space / spanCount
        }

        if (column == spanCount - 1) {
            outRect.right = sideSpace
        } else {
            outRect.right = (column + 1) * space / spanCount
        }

        if (position < spanCount) {
            outRect.top = sideSpace
        }

        outRect.bottom = space
    }
}