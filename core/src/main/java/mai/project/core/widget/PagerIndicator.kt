package mai.project.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import mai.project.core.R
import mai.project.core.extensions.DP
import kotlin.math.abs
import androidx.core.graphics.toColorInt

/**
 * 可應用於 ViewPager 或 RecyclerView 的指示器。
 *
 * 參考於：https://github.com/wching/Android-Indefinite-Pager-Indicator
 *
 * 使用方法：
 * ```
 * // 可見的指示器點點數量
 * app:pagerIndicatorDotCount="5"
 * // 淡出的指示器點點數量
 * app:pagerIndicatorFadingDotCount="1"
 * // 指示器點點的半徑 (預設)
 * app:pagerIndicatorDotRadius="4dp"
 * // 指示器點點的半徑 (選取)
 * app:pagerIndicatorSelectedDotRadius="6dp"
 * // 指示器點點的顏色 (預設)
 * app:pagerIndicatorDotColor="@color/dotDefaultColor"
 * // 指示器點點的顏色 (選取)
 * app:pagerIndicatorSelectedDotColor="@color/dotSelectedColor"
 * // 指示器點點的間距
 * app:pagerIndicatorDotSeparation="10dp"
 * // 是否支援 RTL (預設 false)
 * app:pagerIndicatorSupportRTL="false"
 * // 是否支援垂直滾動 (預設 false)
 * app:pagerIndicatorVerticalSupport=""
 */
class PagerIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle), ViewPager.OnPageChangeListener {

    // 支援的元件 (ViewPager、ViewPager2、RecyclerView)
    private var recyclerView: RecyclerView? = null
    private var viewPager: ViewPager? = null
    private var viewPager2: ViewPager2? = null
    private var internalRecyclerScrollListener: InternalRecyclerScrollListener? = null
    private var internalPageChangeCallback: InternalPageChangeCallback? = null
    private val interpolator = DecelerateInterpolator()

    // 預設參數
    private var dotCount = DEFAULT_DOT_COUNT
    private var fadingDotCount = DEFAULT_FADING_DOT_COUNT
    private var selectedDotRadiusPx = DEFAULT_SELECTED_DOT_RADIUS_DP.DP
    private var dotRadiusPx = DEFAULT_DOT_RADIUS_DP.DP
    private var dotSeparationDistancePx = DEFAULT_DOT_SEPARATION_DISTANCE_DP.DP
    private var supportRtl = false
    private var verticalSupport = false

    // 繪製指示器點點的畫筆
    @ColorInt
    private var dotColor: Int = "#C4C4C4".toColorInt()

    @ColorInt
    private var selectedDotColor: Int = "#F52C56".toColorInt()
    private var selectedDotPaint: Paint
    private var dotPaint: Paint

    /**
     * 目前指示器位置。用於繪製不同大小/顏色的選取點。
     */
    private var selectedItemPosition: Int = 0

    /**
     * 用於反映從一個選取點到下一個選取點的變更/轉換的臨時值。
     */
    private var intermediateSelectedItemPosition: Int = 0

    /**
     * 「ViewPager」或「RecyclerView」的滾動百分比。
     * 用於移動點或縮放淡出的點。
     */
    private var offsetPercent: Float = 0f

    /**
     * 開始繪製指示器點點。
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        (0 until getItemCount())
            .map { position ->
                getDotCoordinate(
                    position = position
                )
            }
            .forEach { coordinate ->
                val (xPosition: Float, yPosition: Float) = getXYPositionsByCoordinate(
                    coordinate = coordinate
                )
                canvas.drawCircle(
                    xPosition,
                    yPosition,
                    getRadius(coordinate = coordinate),
                    getPaint(coordinate = coordinate)
                )
            }
    }

    /**
     * 計算指示器點點的大小和位置。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minimumViewSize = 2 * selectedDotRadiusPx
        if (verticalSupport) {
            setMeasuredDimension(minimumViewSize, getCalculatedWidth())
        } else {
            setMeasuredDimension(getCalculatedWidth(), minimumViewSize)
        }
    }

    /**
     * 綁定 RecyclerView。
     */
    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        removeAllSources()

        this.recyclerView = recyclerView

        InternalRecyclerScrollListener().let { newScrollListener ->
            internalRecyclerScrollListener = newScrollListener
            this.recyclerView?.addOnScrollListener(newScrollListener)
        }
    }

    /**
     * 綁定 ViewPager。
     */
    fun attachToViewPager(viewPager: ViewPager?) {
        removeAllSources()

        this.viewPager = viewPager
        this.viewPager?.addOnPageChangeListener(this)

        selectedItemPosition = viewPager?.currentItem ?: 0
    }

    /**
     * 綁定 ViewPager2。
     */
    fun attachToViewPager2(viewPager2: ViewPager2) {
        removeAllSources()

        this.viewPager2 = viewPager2

        InternalPageChangeCallback().let {
            internalPageChangeCallback = it
            this.viewPager2?.registerOnPageChangeCallback(it)
        }

        selectedItemPosition = this.viewPager2?.currentItem ?: 0
    }

    /**
     * 設定指示器點點數量。
     */
    fun setDotCount(count: Int) {
        dotCount = count
        requestLayout()
        invalidate()
    }

    /**
     * 設定選取的指示器點點。
     */
    fun setSelectedItem(position: Int) {
        selectedItemPosition = position
        intermediateSelectedItemPosition = position
        offsetPercent = 0f
        invalidate()
    }

    /**
     * 設定淡出的指示器點點數量。
     */
    fun setFadingDotCount(count: Int) {
        fadingDotCount = count
        invalidate()
    }

    /**
     * 指示器點點的半徑 (選取)。
     */
    fun setSelectedDotRadius(
        @Dimension(unit = Dimension.DP)
        radius: Int
    ) {
        selectedDotRadiusPx = radius.DP
        invalidate()
    }

    /**
     * 指示器點點的半徑 (預設)。
     */
    fun setDotRadius(
        @Dimension(unit = Dimension.DP)
        radius: Int
    ) {
        dotRadiusPx = radius.DP
        invalidate()
    }

    /**
     * 指示器點點的間距。
     */
    fun setDotSeparationDistance(distance: Int) {
        dotSeparationDistancePx = distance.DP
        invalidate()
    }

    /**
     * 設定是否支援 RTL。
     */
    fun setRTLSupport(supportRTL: Boolean) {
        supportRtl = supportRTL
        invalidate()
    }

    /**
     * 設定是否支援垂直滾動。
     */
    fun setVerticalSupport(verticalSupport: Boolean) {
        this.verticalSupport = verticalSupport
        invalidate()
    }

    /**
     * 設定指示器點點的顏色。
     */
    fun setDotColor(@ColorInt newDotColor: Int) {
        dotColor = newDotColor
        dotPaint.color = dotColor
        invalidate()
    }

    /**
     * 設定指示器點點的顏色 (選取)。
     */
    fun setSelectedDotColor(@ColorInt newSelectedDotColor: Int) {
        selectedDotColor = newSelectedDotColor
        selectedDotPaint.color = selectedDotColor
        invalidate()
    }

    /**
     * 取得預設畫筆設定。
     */
    private fun getDefaultPaintConfig(
        defaultStyle: Paint.Style = Paint.Style.FILL,
        isAntiAliasDefault: Boolean = true,
        @ColorInt defaultColor: Int
    ): Paint = Paint().apply {
        style = defaultStyle
        isAntiAlias = isAntiAliasDefault
        color = defaultColor
    }

    /**
     * 取得指示器點點的 X、Y 座標。
     */
    private fun getXYPositionsByCoordinate(coordinate: Float): Pair<Float, Float> {
        val xPosition: Float
        val yPosition: Float
        if (verticalSupport) {
            xPosition = getDotYCoordinate().toFloat()
            yPosition = height / 2 + coordinate
        } else {
            xPosition = width / 2 + coordinate
            yPosition = getDotYCoordinate().toFloat()
        }
        return Pair(xPosition, yPosition)
    }

    private fun getDotCoordinate(position: Int): Float =
        (position - intermediateSelectedItemPosition) * getDistanceBetweenTheCenterOfTwoDots() +
                (getDistanceBetweenTheCenterOfTwoDots() * offsetPercent)

    /**
     * 獲取一個點的 y 坐標。
     *
     * View 的底部是 y = 0，且點是從中心繪製的，因此 y 坐標僅為半徑。
     */
    private fun getDotYCoordinate(): Int = selectedDotRadiusPx

    /**
     * 計算兩個點中心之間的距離。
     */
    private fun getDistanceBetweenTheCenterOfTwoDots() = 2 * dotRadiusPx + dotSeparationDistancePx

    /**
     * 根據點的位置計算其半徑。
     *
     * 如果位置在一個點長度內，那麼它就是當前選中的點。
     *
     * 如果位置在一個閾值內（非淡出點數量寬度的一半），它是一個正常大小的點。
     *
     * 如果位置超出上述閾值，它是一個淡出的點或不可見。半徑是基於指示器百分比計算的，這個百分比取決於ViewPager/RecyclerView滾動了多遠。
     */
    private fun getRadius(coordinate: Float): Float {
        val coordinateAbs = abs(coordinate)
        // 獲取點開始顯示為淡出點的坐標（x 坐標 > 所有大點寬度的一半）。
        val largeDotThreshold = dotCount.toFloat() / 2 * getDistanceBetweenTheCenterOfTwoDots()
        return when {
            coordinateAbs < getDistanceBetweenTheCenterOfTwoDots() / 2 -> selectedDotRadiusPx.toFloat()
            coordinateAbs <= largeDotThreshold -> dotRadiusPx.toFloat()
            else -> {
                // 確定點距離 View 邊緣的接近程度，用於縮放點的大小。
                val percentTowardsEdge = (coordinateAbs - largeDotThreshold) /
                        (getCalculatedWidth() / 2.01f - largeDotThreshold)
                interpolator.getInterpolation(1 - percentTowardsEdge) * dotRadiusPx
            }
        }
    }

    /**
     * 根據坐標返回點的顏色。
     *
     * 如果位置在 x 或 y = 0 的一個點的長度範圍內，則它是當前選中的點。
     *
     * 其他點將是正常指定的點顏色。
     */
    private fun getPaint(coordinate: Float): Paint = when {
        abs(coordinate) < getDistanceBetweenTheCenterOfTwoDots() / 2 -> selectedDotPaint
        else -> dotPaint
    }

    /**
     * 獲取 View 的計算寬度。
     *
     * 通過可見點的總數（正常和淡出的點）計算。
     */
    private fun getCalculatedWidth(): Int {
        val maxNumVisibleDots = dotCount + 2 * fadingDotCount
        return (maxNumVisibleDots - 1) * getDistanceBetweenTheCenterOfTwoDots() + 2 * dotRadiusPx
    }

    /**
     * 清除所有綁定的元件。
     */
    private fun removeAllSources() {
        internalRecyclerScrollListener?.let {
            recyclerView?.removeOnScrollListener(it)
        }

        this.viewPager?.removeOnPageChangeListener(this)

        internalPageChangeCallback?.let {
            viewPager2?.unregisterOnPageChangeCallback(it)
        }

        recyclerView = null
        viewPager = null
        viewPager2 = null
    }

    /**
     * 獲取元件的項目數量。
     */
    private fun getItemCount(): Int = when {
        recyclerView != null -> recyclerView?.adapter?.itemCount ?: 0
        viewPager != null -> viewPager?.adapter?.count ?: 0
        viewPager2 != null -> viewPager2?.adapter?.itemCount ?: 0
        else -> 0
    }

    /**
     * 檢查是否支援 RTL。
     */
    private fun isRtl() = layoutDirection == LAYOUT_DIRECTION_RTL

    /**
     * 獲取 RTL 位置。
     */
    private fun getRTLPosition(position: Int) = getItemCount() - position - 1

    /**
     * [ViewPager.OnPageChangeListener.onPageScrolled]
     */
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (supportRtl && isRtl()) {
            val currentPosition = getRTLPosition(
                position = position
            )
            selectedItemPosition = currentPosition
            intermediateSelectedItemPosition = currentPosition
            offsetPercent = positionOffset * 1
        } else {
            selectedItemPosition = position
            intermediateSelectedItemPosition = position
            offsetPercent = positionOffset * -1
        }
        invalidate()
    }

    /**
     * [ViewPager.OnPageChangeListener.onPageSelected]
     */
    override fun onPageSelected(position: Int) {
        intermediateSelectedItemPosition = selectedItemPosition
        selectedItemPosition = if (supportRtl && isRtl()) {
            getRTLPosition(
                position = position
            )
        } else {
            position
        }
        invalidate()
    }

    /**
     * [ViewPager.OnPageChangeListener.onPageScrollStateChanged]
     */
    override fun onPageScrollStateChanged(state: Int) {
        // nothing ...
    }

    /**
     * [RecyclerView.OnScrollListener]
     */
    internal inner class InternalRecyclerScrollListener : RecyclerView.OnScrollListener() {

        /**
         * RecyclerView中之前的可見的子頁面。
         *
         * 用於區分當前最可見的子頁面，以正確確定當前選中的項目和滾動的百分比。
         */
        private var previousMostVisibleChild: View? = null

        /**
         * 根據子 View 持有者的 View 可見百分比來確定當前選中的位置。
         *
         * 使用這個百分比來計算用於縮放點的 offsetPercentage。
         */
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            val view = getMostVisibleChild()
            if (view != null) {
                setIntermediateSelectedItemPosition(
                    mostVisibleChild = view
                )
                offsetPercent = view.left.toFloat() / view.measuredWidth
            }

            with(recyclerView.layoutManager as LinearLayoutManager) {
                val visibleItemPosition =
                    if (dx >= 0) findLastVisibleItemPosition() else findFirstVisibleItemPosition()

                if (previousMostVisibleChild !== findViewByPosition(visibleItemPosition)) {
                    selectedItemPosition = intermediateSelectedItemPosition
                }
            }

            previousMostVisibleChild = view
            invalidate()
        }

        private fun getMostVisibleChild(): View? {
            var mostVisibleChild: View? = null
            var mostVisibleChildPercent = 0f
            for (i in recyclerView?.layoutManager?.childCount!! - 1 downTo 0) {
                val child = recyclerView?.layoutManager?.getChildAt(i)
                if (child != null) {
                    val percentVisible = calculatePercentVisible(
                        child = child
                    )
                    if (percentVisible >= mostVisibleChildPercent) {
                        mostVisibleChildPercent = percentVisible
                        mostVisibleChild = child
                    }
                }
            }

            return mostVisibleChild
        }

        private fun calculatePercentVisible(child: View): Float {
            val left = child.left
            val right = child.right
            val width = child.width

            return when {
                left < 0 -> right / width.toFloat()
                right > getWidth() -> (getWidth() - left) / width.toFloat()
                else -> 1f
            }
        }

        private fun setIntermediateSelectedItemPosition(mostVisibleChild: View) {
            recyclerView?.findContainingViewHolder(mostVisibleChild)?.adapterPosition
                ?.let { position ->
                    intermediateSelectedItemPosition = if (isRtl() && !verticalSupport) {
                        getRTLPosition(
                            position = position
                        )
                    } else {
                        position
                    }
                }
        }
    }

    /**
     * [ViewPager2.OnPageChangeCallback]
     */
    internal inner class InternalPageChangeCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            this@PagerIndicator.onPageScrolled(
                position,
                positionOffset,
                positionOffsetPixels
            )
        }

        override fun onPageSelected(position: Int) {
            this@PagerIndicator.onPageSelected(position)
        }
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.PagerIndicator, 0, 0).apply {
            try {
                dotCount = getInteger(
                    R.styleable.PagerIndicator_pagerIndicatorDotCount, DEFAULT_DOT_COUNT
                )

                fadingDotCount = getInt(
                    R.styleable.PagerIndicator_pagerIndicatorFadingDotCount,
                    DEFAULT_FADING_DOT_COUNT
                )

                dotRadiusPx = getDimensionPixelSize(
                    R.styleable.PagerIndicator_pagerIndicatorDotRadius, dotRadiusPx
                )

                selectedDotRadiusPx = getDimensionPixelSize(
                    R.styleable.PagerIndicator_pagerIndicatorSelectedDotRadius, selectedDotRadiusPx
                )

                dotColor = getColor(
                    R.styleable.PagerIndicator_pagerIndicatorDotColor, dotColor
                )

                selectedDotColor = getColor(
                    R.styleable.PagerIndicator_pagerIndicatorSelectedDotColor, selectedDotColor
                )

                dotSeparationDistancePx = getDimensionPixelSize(
                    R.styleable.PagerIndicator_pagerIndicatorDotSeparation, dotSeparationDistancePx
                )

                supportRtl = getBoolean(
                    R.styleable.PagerIndicator_pagerIndicatorSupportRTL, false
                )

                verticalSupport = getBoolean(
                    R.styleable.PagerIndicator_pagerIndicatorVerticalSupport, false
                )
            } finally {
                recycle()

                selectedDotPaint = getDefaultPaintConfig(defaultColor = selectedDotColor)
                dotPaint = getDefaultPaintConfig(defaultColor = dotColor)
            }
        }
    }

    private companion object {
        private const val DEFAULT_DOT_COUNT = 5
        private const val DEFAULT_FADING_DOT_COUNT = 1
        private const val DEFAULT_DOT_RADIUS_DP = 4
        private const val DEFAULT_SELECTED_DOT_RADIUS_DP = 6
        private const val DEFAULT_DOT_SEPARATION_DISTANCE_DP = 10
    }
}