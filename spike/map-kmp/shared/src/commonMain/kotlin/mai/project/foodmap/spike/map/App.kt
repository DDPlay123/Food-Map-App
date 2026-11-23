package mai.project.foodmap.spike.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private val Brand = Color(MapDefaults.BRAND_COLOR)

/**
 * 驗證用畫面：地圖 + 疊在上方的控制列與橫向餐廳列表 + 群集清單 BottomSheet。
 * 互動規則照搬正式 App 的 MapTabFragment。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (darkTheme) darkColorScheme(primary = Brand) else lightColorScheme(primary = Brand)

    MaterialTheme(colorScheme = colors) {
        val markers = remember { SampleData.restaurants }
        val route = remember { PolylineDecoder.decode(SampleData.ENCODED_ROUTE) }

        var selectedId by remember { mutableStateOf<String?>(null) }
        var showRoute by remember { mutableStateOf(false) }
        var cameraRequest by remember { mutableStateOf<CameraRequest?>(null) }
        var clusterSheet by remember { mutableStateOf<List<RestaurantMarker>?>(null) }
        var nextToken by remember { mutableLongStateOf(0L) }
        var scrollingFromMarker by remember { mutableStateOf(false) }

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        fun focus(marker: RestaurantMarker) {
            selectedId = marker.id
            cameraRequest = CameraRequest.Focus(marker.position, zoom = null, token = ++nextToken)
        }

        fun focusAndSyncList(marker: RestaurantMarker) {
            focus(marker)
            val index = markers.indexOf(marker)
            if (index >= 0) scope.launch {
                scrollingFromMarker = true
                listState.animateScrollToItem(index)
            }
        }

        // 列表停止捲動時，鏡頭移到目前最左邊的那家店（對應 MapTabFragment 的 onScrollStateChanged）
        LaunchedEffect(listState) {
            snapshotFlow { listState.isScrollInProgress }
                .drop(1)
                .filter { inProgress -> !inProgress }
                .collect {
                    if (scrollingFromMarker) {
                        scrollingFromMarker = false
                    } else {
                        markers.getOrNull(listState.firstVisibleItemIndex)?.let(::focus)
                    }
                }
        }

        Box(Modifier.fillMaxSize()) {
            RestaurantMap(
                markers = markers,
                route = if (showRoute) route else emptyList(),
                selectedMarkerId = selectedId,
                cameraRequest = cameraRequest,
                darkTheme = darkTheme,
                initialCenter = SampleData.center,
                initialZoom = 15f,
                onMarkerClick = ::focusAndSyncList,
                onClusterAtMaxZoom = { clusterSheet = it },
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = showRoute,
                        onClick = {
                            showRoute = !showRoute
                            if (showRoute) cameraRequest = CameraRequest.Fit(route, paddingDp = 64, token = ++nextToken)
                        },
                        label = { Text("路線") },
                    )
                    AssistChip(
                        onClick = {
                            cameraRequest = CameraRequest.Fit(markers.map { it.position }, paddingDp = 48, token = ++nextToken)
                        },
                        label = { Text("顯示全部") },
                    )
                }

                LazyRow(
                    state = listState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(markers, key = { it.id }) { marker ->
                        RestaurantCard(
                            marker = marker,
                            selected = marker.id == selectedId,
                            onClick = { focus(marker) },
                        )
                    }
                }
            }

            clusterSheet?.let { list ->
                ModalBottomSheet(onDismissRequest = { clusterSheet = null }) {
                    Text(
                        text = "這個位置有 ${list.size} 家餐廳",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                    LazyColumn {
                        items(list, key = { it.id }) { marker ->
                            ListItem(
                                headlineContent = { Text(marker.name) },
                                supportingContent = { Text(ratingText(marker.rating)) },
                                modifier = Modifier.clickable {
                                    clusterSheet = null
                                    focusAndSyncList(marker)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantCard(marker: RestaurantMarker, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = if (selected) BorderStroke(2.dp, Brand) else null,
        modifier = Modifier.width(220.dp),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = marker.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = ratingText(marker.rating), style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * 地圖上的餐廳 Marker 外觀，對應正式 App 的 layout_restaurant_marker。
 * Android 直接把這個 Composable 當 Marker；iOS 的 Marker 由 Swift 端的原生 UIView 繪製，外觀規格相同。
 */
@Composable
fun RestaurantPin(marker: RestaurantMarker, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Brand else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Brand),
        shadowElevation = 2.dp,
    ) {
        Text(
            text = marker.rating?.let { "${marker.name}  ★$it" } ?: marker.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

internal fun ratingText(rating: Double?): String = rating?.let { "★ $it" } ?: "尚無評分"
