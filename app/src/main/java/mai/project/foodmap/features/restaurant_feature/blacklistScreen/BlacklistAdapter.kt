package mai.project.foodmap.features.restaurant_feature.blacklistScreen

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.transform.RoundedCornersTransformation
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.databinding.ItemRestaurantListBinding
import mai.project.foodmap.domain.models.MyBlacklistResult
import java.util.Locale

class BlacklistAdapter : ListAdapter<MyBlacklistResult, ViewHolder>(DiffUtilCallback) {

    var onItemClick: ((MyBlacklistResult) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        BlacklistViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is BlacklistViewHolder) holder.bind(getItem(position), onItemClick)
    }

    private class BlacklistViewHolder(
        private val binding: ItemRestaurantListBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: MyBlacklistResult,
            onItemClick: ((MyBlacklistResult) -> Unit)?
        ) = with(binding) {
            ImageLoaderUtil.loadImage(
                imageView = imgPhoto,
                resource = item.photos.firstOrNull().orEmpty(),
                imageType = ImageType.DEFAULT,
                transformation = RoundedCornersTransformation(25f)
            )

            tvName.text = item.name
            tvRating.text = "${item.ratingStar}"
            tvRatingTotal.text = String.format(Locale.getDefault(), "(%d)", item.ratingTotal)
            tvAddress.text = item.address

            imgFavorite.isVisible = false
            tvDistance.isVisible = false

            root.onClick { onItemClick?.invoke(item) }
        }

        companion object {
            fun from(parent: ViewGroup) = BlacklistViewHolder(
                parent.inflateBinding(ItemRestaurantListBinding::inflate)
            )
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<MyBlacklistResult>() {
            override fun areItemsTheSame(
                oldItem: MyBlacklistResult,
                newItem: MyBlacklistResult
            ): Boolean = oldItem.placeId == newItem.placeId

            override fun areContentsTheSame(
                oldItem: MyBlacklistResult,
                newItem: MyBlacklistResult
            ): Boolean = oldItem == newItem
        }
    }
}