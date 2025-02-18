package mai.project.foodmap.features.home_features.mapTabScreen.utils

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MyClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<RestaurantClusterItem>
) : DefaultClusterRenderer<RestaurantClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: RestaurantClusterItem, markerOptions: MarkerOptions) {
        item.marker?.let { bitmap ->
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        }
        markerOptions.title(item.title)
        markerOptions.snippet(item.snippet)
        markerOptions.zIndex(item.zIndex ?: 0f)
    }
}
