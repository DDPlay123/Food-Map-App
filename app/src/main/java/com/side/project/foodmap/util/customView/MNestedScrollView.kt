package com.side.project.foodmap.util.customView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import kotlin.math.abs

/**
 * 避免 NestedScrollView 與 ViewPager2 滑動衝突
 */
class MNestedScrollView : NestedScrollView {

    private var xDistance: Float = 0.0f
    private var yDistance: Float = 0.0f
    private var xLast: Float = 0.0f
    private var yLast: Float = 0.0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xDistance = 0.0f
                yDistance = 0.0f
                xLast = event.x
                yLast = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val curX = event.x
                val curY = event.y
                xDistance += abs(curX - curY)
                yDistance += abs(curY - curX)
                xLast = curX
                yLast = curY

                if (xDistance > yDistance)
                    return true
            }
            else -> Unit
        }
        return super.onInterceptHoverEvent(event)
    }
}