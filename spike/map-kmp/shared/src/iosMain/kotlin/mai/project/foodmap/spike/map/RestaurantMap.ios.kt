package mai.project.foodmap.spike.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView

/** 由 MainViewController 提供 Swift 端的地圖工廠。 */
val LocalNativeMapFactory = staticCompositionLocalOf<NativeMapFactory> {
    error("沒有提供 NativeMapFactory，請透過 MainViewController(mapFactory) 啟動")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun RestaurantMap(
    markers: List<RestaurantMarker>,
    route: List<GeoPoint>,
    selectedMarkerId: String?,
    cameraRequest: CameraRequest?,
    darkTheme: Boolean,
    initialCenter: GeoPoint,
    initialZoom: Float,
    onMarkerClick: (RestaurantMarker) -> Unit,
    onClusterAtMaxZoom: (List<RestaurantMarker>) -> Unit,
    modifier: Modifier,
) {
    val factory = LocalNativeMapFactory.current
    val nativeMap = remember(factory) { factory.create(initialCenter, initialZoom, MapDefaults.MAX_ZOOM) }

    val markersById by rememberUpdatedState(markers.associateBy { it.id })
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnClusterAtMaxZoom by rememberUpdatedState(onClusterAtMaxZoom)

    DisposableEffect(nativeMap) {
        nativeMap.setListener(object : NativeMapListener {
            override fun onMarkerClick(markerId: String) {
                markersById[markerId]?.let(currentOnMarkerClick)
            }

            override fun onClusterAtMaxZoom(markerIds: List<String>) {
                currentOnClusterAtMaxZoom(markerIds.mapNotNull { markersById[it] })
            }
        })
        onDispose { nativeMap.setListener(null) }
    }

    LaunchedEffect(nativeMap, markers, selectedMarkerId) { nativeMap.setMarkers(markers, selectedMarkerId) }
    LaunchedEffect(nativeMap, route) { nativeMap.setRoute(route) }
    LaunchedEffect(nativeMap, darkTheme) { nativeMap.setStyle(if (darkTheme) MapStyles.NIGHT_JSON else null) }
    LaunchedEffect(nativeMap, cameraRequest) {
        when (val request = cameraRequest) {
            null -> Unit
            is CameraRequest.Focus -> nativeMap.focus(request.target, request.zoom ?: 0f)
            is CameraRequest.Fit -> if (request.points.isNotEmpty()) {
                nativeMap.fit(request.points, request.paddingDp.toDouble())
            }
        }
    }

    UIKitView(
        factory = { nativeMap.view },
        modifier = modifier,
        properties = UIKitInteropProperties(
            // 地圖需要立即取得所有觸控（拖曳、縮放、旋轉），不和 Compose 協調手勢
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
    )
}
