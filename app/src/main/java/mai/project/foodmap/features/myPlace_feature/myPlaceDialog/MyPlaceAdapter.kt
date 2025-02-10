package mai.project.foodmap.features.myPlace_feature.myPlaceDialog

import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.transform.RoundedCornersTransformation
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.BuildConfig
import mai.project.foodmap.databinding.ItemMyPlaceBinding
import mai.project.foodmap.domain.models.MyPlaceResult
import java.util.Locale

class MyPlaceAdapter(
    private val imageLoaderUtil: ImageLoaderUtil
) : ListAdapter<MyPlaceResult, ViewHolder>(DiffUtilCallback) {

    var onClickedPlace: ((MyPlaceResult) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        MyPlaceViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is MyPlaceViewHolder) holder.bind(
            item = getItem(position),
            selectedPlaceId = selectedPlaceId,
            imageLoaderUtil = imageLoaderUtil,
            onClickedPlace = onClickedPlace
        )
    }

    private var selectedPlaceId = ""

    fun submitList(
        list: List<MyPlaceResult>,
        selectedPlaceId: String,
        commitCallback: Runnable
    ) {
        this.selectedPlaceId = selectedPlaceId
        super.submitList(list, commitCallback)
    }

    private class MyPlaceViewHolder(
        private val binding: ItemMyPlaceBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: MyPlaceResult,
            selectedPlaceId: String,
            imageLoaderUtil: ImageLoaderUtil,
            onClickedPlace: ((MyPlaceResult) -> Unit)? = null
        ) = with(binding) {
            imgMaps.isVisible = item.placeId == selectedPlaceId
            val photo = if (item.placeId == selectedPlaceId) {
                "https://maps.google.com/maps/api/staticmap?center=${item.lat},${item.lng}" +
                        "&zoom=18&size=1600x400&sensor=false&markers=color:red%7Clabel:O%7C${item.lat},${item.lng}" +
                        "&key=${BuildConfig.GOOGLE_API_KEY}"
            } else ""
            imageLoaderUtil.loadImage(
                imageView = imgMaps,
                resource = photo,
                imageType = ImageType.DEFAULT,
                transformation = RoundedCornersTransformation(25f)
            )
            tvAddress.text = String.format(Locale.getDefault(), "%s\n%s", item.name, item.address)
            checkbox.isChecked = item.placeId == selectedPlaceId

            clRoot.onClick { onClickedPlace?.invoke(item) }
        }

        companion object {
            fun from(parent: ViewGroup) = MyPlaceViewHolder(
                parent.inflateBinding(ItemMyPlaceBinding::inflate)
            )
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<MyPlaceResult>() {
            override fun areItemsTheSame(
                oldItem: MyPlaceResult,
                newItem: MyPlaceResult
            ): Boolean = oldItem.placeId == newItem.placeId

            override fun areContentsTheSame(
                oldItem: MyPlaceResult,
                newItem: MyPlaceResult
            ): Boolean = oldItem == newItem
        }
    }
}