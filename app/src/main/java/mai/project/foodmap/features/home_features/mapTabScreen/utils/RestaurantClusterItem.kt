package mai.project.foodmap.features.home_features.mapTabScreen.utils

import android.graphics.Bitmap
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import kotlinx.parcelize.Parcelize
import mai.project.foodmap.domain.models.RestaurantResult

@Parcelize
data class RestaurantClusterItem(
    private val position: LatLng,
    private val title: String?,
    private val snippet: String?,
    private val zIndex: Float?,
    val marker: Bitmap?,
    val data: RestaurantResult
) : ClusterItem, Parcelable {
    override fun getPosition(): LatLng = position
    override fun getTitle(): String? = title
    override fun getSnippet(): String? = snippet
    override fun getZIndex(): Float? = zIndex
}
