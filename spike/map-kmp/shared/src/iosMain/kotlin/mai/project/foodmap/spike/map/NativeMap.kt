package mai.project.foodmap.spike.map

import platform.UIKit.UIView

/**
 * iOS 地圖的原生實作介面，由 Swift 端（iosApp/iosApp/GoogleMapView.swift）實作。
 *
 * Kotlin 不直接綁定 Google Maps iOS SDK（不用 cinterop），而是由 Swift 實作這個介面後傳進來。
 * 好處是 Google Maps SDK 只需要在 Xcode 用 Swift Package Manager 加入，Kotlin 端的建置不受影響。
 */
interface NativeMapView {
    val view: UIView

    fun setListener(listener: NativeMapListener?)

    fun setMarkers(markers: List<RestaurantMarker>, selectedMarkerId: String?)

    fun setRoute(points: List<GeoPoint>)

    /** [zoom] 小於等於 0 時維持目前的縮放。 */
    fun focus(target: GeoPoint, zoom: Float)

    fun fit(points: List<GeoPoint>, paddingPoints: Double)

    /** 傳 null 代表使用預設（淺色）樣式。 */
    fun setStyle(styleJson: String?)
}

interface NativeMapListener {
    fun onMarkerClick(markerId: String)

    fun onClusterAtMaxZoom(markerIds: List<String>)
}

interface NativeMapFactory {
    fun create(initialCenter: GeoPoint, initialZoom: Float, maxZoom: Float): NativeMapView
}
