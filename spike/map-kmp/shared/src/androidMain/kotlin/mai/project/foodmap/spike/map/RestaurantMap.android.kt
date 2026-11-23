package mai.project.foodmap.spike.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/** android-maps-utils 6.x 的 ClusterItem 是 Kotlin interface，以屬性實作。 */
private class RestaurantClusterItem(
    val marker: RestaurantMarker,
    val selected: Boolean,
) : ClusterItem {
    override val position: LatLng = marker.position.toLatLng()
    override val title: String = marker.name
    override val snippet: String = marker.id
    override val zIndex: Float = if (selected) 1f else 0f
}

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

private fun boundsOf(points: List<LatLng>): LatLngBounds =
    LatLngBounds.builder().apply { points.forEach(::include) }.build()

// maps-compose 的群集 API（Clustering）仍標示為實驗性；正式 App 遷移時需留意版本升級的變動
@OptIn(MapsComposeExperimentalApi::class)
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
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter.toLatLng(), initialZoom)
    }
    val scope = rememberCoroutineScope()
    val clusterPaddingPx = with(LocalDensity.current) { 80.dp.roundToPx() }
    val requestPaddingPx = with(LocalDensity.current) { ((cameraRequest as? CameraRequest.Fit)?.paddingDp ?: 0).dp.roundToPx() }

    // 選取狀態改變時重建項目，讓被選取的 Marker 重新繪製
    val items = remember(markers, selectedMarkerId) {
        markers.map { RestaurantClusterItem(it, selected = it.id == selectedMarkerId) }
    }
    val routeLatLngs = remember(route) { route.map { it.toLatLng() } }
    val properties = remember(darkTheme) {
        MapProperties(
            mapStyleOptions = if (darkTheme) MapStyleOptions(MapStyles.NIGHT_JSON) else null,
            maxZoomPreference = MapDefaults.MAX_ZOOM,
        )
    }

    LaunchedEffect(cameraRequest) {
        cameraState.apply(cameraRequest, requestPaddingPx)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        properties = properties,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
    ) {
        Clustering(
            items = items,
            onClusterClick = { cluster ->
                val clusterMarkers = cluster.items.map { it.marker }
                if (cameraState.position.zoom >= MapDefaults.MAX_ZOOM - 0.5f) {
                    onClusterAtMaxZoom(clusterMarkers)
                } else {
                    scope.launch {
                        cameraState.animate(
                            CameraUpdateFactory.newLatLngBounds(boundsOf(cluster.items.map { it.position }), clusterPaddingPx),
                        )
                    }
                }
                true
            },
            onClusterItemClick = { item ->
                onMarkerClick(item.marker)
                true
            },
            clusterItemContent = { item -> RestaurantPin(item.marker, item.selected) },
        )

        if (routeLatLngs.size >= 2) {
            Polyline(points = routeLatLngs, color = Color(MapDefaults.BRAND_COLOR), width = 14f)
        }
    }
}

private suspend fun CameraPositionState.apply(request: CameraRequest?, paddingPx: Int) {
    when (request) {
        null -> Unit
        is CameraRequest.Focus -> animate(
            if (request.zoom != null) {
                CameraUpdateFactory.newLatLngZoom(request.target.toLatLng(), request.zoom)
            } else {
                CameraUpdateFactory.newLatLng(request.target.toLatLng())
            },
        )
        is CameraRequest.Fit -> if (request.points.isNotEmpty()) {
            animate(CameraUpdateFactory.newLatLngBounds(boundsOf(request.points.map { it.toLatLng() }), paddingPx))
        }
    }
}
