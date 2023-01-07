package com.side.project.foodmap.util.animPolyline

import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.addInfoWindow
import com.side.project.foodmap.helper.getTime
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

    fun addInfoWindow(context: Context, distance: Int, duration: Int) {
        val pointsOnLine = this.points.size
        val infoLatLng = this.points[(pointsOnLine / 2)]
        val invisibleMarker = BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        val marker = map.addMarker(
            MarkerOptions()
                .position(infoLatLng)
                .title(String.format(
                    context.getString(if (distance < 1000) R.string.text_number_int_meter else R.string.text_number_int_kilometer),
                    if (distance < 1000) distance else distance / 1000))
                .snippet(duration.getTime())
                .alpha(0f)
                .icon(invisibleMarker)
                .anchor(0f, 0f)
        )
        marker?.showInfoWindow()
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