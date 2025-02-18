package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.databinding.ItemGoogleReviewBinding
import mai.project.foodmap.domain.models.RestaurantDetailResult

class GoogleReviewAdapter : ListAdapter<RestaurantDetailResult.Review, ViewHolder>(DiffUtilCallback) {

    var onAvatarClick: ((url: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        GoogleReviewViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is GoogleReviewViewHolder) holder.bind(getItem(position), onAvatarClick)
    }

    private class GoogleReviewViewHolder(
        private val binding: ItemGoogleReviewBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: RestaurantDetailResult.Review,
            onAvatarClick: ((url: String) -> Unit)?
        ) = with(binding) {
            ImageLoaderUtil.loadImage(
                imageView = imgAvatar,
                resource = item.profilePhotoUrl,
                imageType = ImageType.PERSON
            )
            tvName.text = item.authorName
            rating.rating = item.rating.toFloat()
            tvTime.text = item.time
            tvMessage.originalText = item.text

            imgAvatar.onClick { onAvatarClick?.invoke(item.authorUrl) }
        }

        companion object {
            fun from(parent: ViewGroup) = GoogleReviewViewHolder(
                parent.inflateBinding(ItemGoogleReviewBinding::inflate)
            )
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<RestaurantDetailResult.Review>() {
            override fun areItemsTheSame(
                oldItem: RestaurantDetailResult.Review,
                newItem: RestaurantDetailResult.Review
            ): Boolean = oldItem.rating == newItem.rating

            override fun areContentsTheSame(
                oldItem: RestaurantDetailResult.Review,
                newItem: RestaurantDetailResult.Review
            ): Boolean = oldItem == newItem
        }
    }
}