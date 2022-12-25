package com.side.project.foodmap.util.animPolyline

import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.side.project.foodmap.util.animPolyline.PolylineOptionsExtensions.copyPolylineOptions
import com.side.project.foodmap.util.animPolyline.PolylineOptionsExtensions.toPolylineOptions

// Reference：https://github.com/P72B/PolylineAnimator/blob/main/animation/src/main/java/de/p72b/maps/animation/AnimatedPolyline.kt
class AnimatedPolyline(
    private var map: GoogleMap,
    private var points: List<LatLng>,
    private var polylineOptions: PolylineOptions = PolylineOptions(),
    duration: Long = 3000,
    interpolator: TimeInterpolator? = null,
    private val animatorListenerAdapter: AnimatorListenerAdapter? = null
) : ValueAnimator.AnimatorUpdateListener {
    private var renderedPolyline: Polyline? = null
    private lateinit var legs: List<Double>
    private var totalPathDistance: Double = 0.0
    private var animator: ValueAnimator = ValueAnimator.ofFloat(0f, 100f)

    init {
        animator.duration = duration
        interpolator?.let {
            animator.interpolator = it
        }
        animator.addUpdateListener(this)
        animatorListenerAdapter?.let {
            animator.addListener(it)
        }
    }

    fun replacePoints(pointList: List<LatLng>) {
        points = pointList
        val polylineOptions = polylineOptions.toPolylineOptions(pointList)
        renderPolylineOnMap(polylineOptions)
    }

    fun start() {
        legs = CalculationHelper.calculateLegsLengths(points)
        totalPathDistance = legs.sum()
        animatorListenerAdapter?.let {
            if (animator.listeners == null || !animator.listeners.contains(it)) {
                animator.addListener(it)
                animator.addUpdateListener(this)
            }
        }
        animator.start()
    }

    fun startWithDelay(milliseconds: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            start()
        }, milliseconds)
    }

    fun remove() {
        animator.removeUpdateListener(this)
        animatorListenerAdapter?.let {
            animator.removeListener(it)
        }
        animator.cancel()
        renderedPolyline?.remove()
    }

    private fun renderPolylineOnMap(polylineOptions: PolylineOptions) {
        val newPolyline = map.addPolyline(polylineOptions)
        renderedPolyline?.remove()
        renderedPolyline = newPolyline
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val fraction = animator.animatedValue as Float
        val pathSection = totalPathDistance * fraction / 100
        renderPolylineOnMap(
            CalculationHelper.polylineUntilSection(
                points, legs, pathSection, polylineOptions.copyPolylineOptions()
            )
        )
    }
}