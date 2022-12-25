package com.side.project.foodmap.util.animPolyline

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

// Reference：https://github.com/P72B/PolylineAnimator/blob/main/animation/src/main/java/de/p72b/maps/animation/util/PolylineOptionsExtensions.kt
object PolylineOptionsExtensions {

    fun PolylineOptions.toPolylineOptions(points: List<LatLng>): PolylineOptions {
        val polylineOptions = this.copyPolylineOptions()
        polylineOptions.addAll(points)
        return polylineOptions
    }

    fun PolylineOptions.copyPolylineOptions(): PolylineOptions {
        return PolylineOptions()
            .color(this.color)
            .width(this.width)
            .startCap(this.startCap)
            .endCap(this.endCap)
            .clickable(this.isClickable)
            .jointType(this.jointType)
            .visible(this.isVisible)
            .pattern(this.pattern)
    }
}