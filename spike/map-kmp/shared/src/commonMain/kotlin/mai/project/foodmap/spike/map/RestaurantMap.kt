package mai.project.foodmap.spike.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 共用的地圖元件。Android 用 maps-compose 實作，iOS 用 Swift 包裝的 GMSMapView 實作。
 *
 * 行為與正式 App 的 MapTabFragment 一致：
 * - 點 Marker：呼叫 [onMarkerClick]（由呼叫端決定移動鏡頭、同步列表）
 * - 點群集：還能放大時縮放到群集範圍；已達 [MapDefaults.MAX_ZOOM] 時呼叫 [onClusterAtMaxZoom]
 * - [route] 非空時畫出路線
 * - [cameraRequest] 改變時移動鏡頭
 */
@Composable
expect fun RestaurantMap(
    markers: List<RestaurantMarker>,
    route: List<GeoPoint>,
    selectedMarkerId: String?,
    cameraRequest: CameraRequest?,
    darkTheme: Boolean,
    initialCenter: GeoPoint,
    initialZoom: Float,
    onMarkerClick: (RestaurantMarker) -> Unit,
    onClusterAtMaxZoom: (List<RestaurantMarker>) -> Unit,
    modifier: Modifier = Modifier,
)
