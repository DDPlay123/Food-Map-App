package mai.project.foodmap.features.dialogs_features.selector

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectorModel(
    val id: Int,
    @DrawableRes
    val iconResId: Int = 0,
    val content: String
) : Parcelable
