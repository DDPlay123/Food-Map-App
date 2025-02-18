package mai.project.core.widget.recyclerView_decorations

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

/**
 * RecyclerView 滑動縮放效果
 *
 * @param maxScale 最大縮放比例
 * @param minScale 最小縮放比例
 */
class ScaleItemDecoration(
    private val maxScale: Float,
    private val minScale: Float
) : RecyclerView.ItemDecoration() {
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // RecyclerView 的中心點 X 座標
        val centerX = parent.width / 2
        // 設定 item 最大影響範圍（超過此範圍不影響縮放）
        val d1 = centerX * 0.75f

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            // 計算 item 中心點 X 座標
            val childCenterX = (child.left + child.right) / 2
            // 計算 item 與中心點的距離（最大為 d1）
            val d = min(d1, abs(centerX - childCenterX).toFloat())

            // 透過線性插值計算 scale 值，使其根據距離中心點的遠近，進行動態縮放
            val scale = maxScale + (minScale - maxScale) * (d / d1)
            child.scaleX = scale
            child.scaleY = scale
        }
    }
}
