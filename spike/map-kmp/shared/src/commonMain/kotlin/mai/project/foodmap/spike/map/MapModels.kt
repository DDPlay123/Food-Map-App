package mai.project.foodmap.spike.map

/**
 * 經緯度。不用 Google Maps 的 LatLng，讓共用程式碼不依賴任何平台型別。
 */
data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * 地圖上的一家餐廳，對應正式 App 的 RestaurantClusterItem。
 */
data class RestaurantMarker(
    val id: String,
    val name: String,
    val position: GeoPoint,
    val rating: Double?,
)

/**
 * 要求地圖移動鏡頭。
 *
 * 每個要求帶一個遞增的 [token]：同一個目標連續要求兩次（例如重複點同一家店）時，
 * 兩個實例不相等，地圖仍會再移動一次。
 */
sealed interface CameraRequest {
    val token: Long

    /** 移到某一點；[zoom] 為 null 時維持目前的縮放。 */
    data class Focus(val target: GeoPoint, val zoom: Float?, override val token: Long) : CameraRequest

    /** 縮放到剛好包含所有點。 */
    data class Fit(val points: List<GeoPoint>, val paddingDp: Int, override val token: Long) : CameraRequest
}

object MapDefaults {
    /**
     * 兩個平台統一的最大縮放。正式 App 比較的是 GoogleMap.maxZoomLevel，
     * 但它會隨地區改變；固定數值能讓兩個平台的群集行為一致。
     */
    const val MAX_ZOOM = 20f

    /** 品牌主色，對應 App 的 colors.xml `primary`。 */
    const val BRAND_COLOR = 0xFFF52C56
}
