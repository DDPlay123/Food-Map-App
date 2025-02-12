package mai.project.foodmap.features.home_features.homeTabScreen

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.transform.RoundedCornersTransformation
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.R
import mai.project.foodmap.databinding.ItemDrawCardBinding
import mai.project.foodmap.domain.models.RestaurantResult
import java.util.Locale

class DrawCardAdapter(
    private val imageLoaderUtil: ImageLoaderUtil
) : ListAdapter<RestaurantResult, ViewHolder>(DiffUtilCallback) {

    var onItemClick: ((RestaurantResult) -> Unit)? = null

    var onFavoriteClick: ((RestaurantResult) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        DrawCardViewModel.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is DrawCardViewModel) holder.bind(
            item = getItem(position),
            imageLoaderUtil = imageLoaderUtil,
            onItemClick = onItemClick,
            onFavoriteClick = onFavoriteClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty() && holder is DrawCardViewModel) {
            holder.bind(
                item = getItem(position),
                imageLoaderUtil = imageLoaderUtil,
                onItemClick = onItemClick,
                onFavoriteClick = onFavoriteClick
            )
        } else {
            for (payload in payloads) {
                if (payload is Bundle &&
                    payload.containsKey(KEY_IS_FAVORITE) &&
                    holder is DrawCardViewModel
                ) {
                    val isFavorite = payload.getBoolean(KEY_IS_FAVORITE)
                    holder.updateFavorite(isFavorite)
                }
            }
        }
    }

    private class DrawCardViewModel(
        private val binding: ItemDrawCardBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: RestaurantResult,
            imageLoaderUtil: ImageLoaderUtil,
            onItemClick: ((RestaurantResult) -> Unit)?,
            onFavoriteClick: ((RestaurantResult) -> Unit)?
        ) = with(binding) {
            val context = root.context
            imageLoaderUtil.loadImage(
                imageView = imgPhoto,
                resource = item.photos.firstOrNull().orEmpty(),
                imageType = ImageType.DEFAULT,
                transformation = RoundedCornersTransformation(25f)
            )

            tvName.text = item.name
            tvRating.text = "${item.ratingStar}"
            rating.rating = item.ratingStar
            tvRatingTotal.text = String.format(Locale.getDefault(), "(%d)", item.ratingTotal)
            tvAddress.text = item.address

            if (item.isFavorite) {
                imgFavorite.setImageResource(R.drawable.vector_favorite)
            } else {
                imgFavorite.setImageResource(R.drawable.vector_favorite_border)
            }

            // TODO 距離藥用當前位置計算
            tvDistance.text = String.format(
                Locale.getDefault(),
                if (item.distance < 1000) context.getString(R.string.format_number_meter) else context.getString(R.string.format_number_kilometer),
                if (item.distance < 1000) item.distance else item.distance / 1000
            )

            root.onClick { onItemClick?.invoke(item) }
            imgFavorite.onClick(anim = true) { onFavoriteClick?.invoke(item) }
        }

        fun updateFavorite(isFavorite: Boolean) = with(binding.imgFavorite) {
            if (isFavorite) {
                setImageResource(R.drawable.vector_favorite)
            } else {
                setImageResource(R.drawable.vector_favorite_border)
            }
        }

        companion object {
            fun from(parent: ViewGroup) = DrawCardViewModel(
                parent.inflateBinding(ItemDrawCardBinding::inflate)
            )
        }
    }

    companion object {
        private const val KEY_IS_FAVORITE = "KEY_IS_FAVORITE"

        private val DiffUtilCallback = object : DiffUtil.ItemCallback<RestaurantResult>() {
            override fun areItemsTheSame(
                oldItem: RestaurantResult,
                newItem: RestaurantResult
            ): Boolean = oldItem.placeId == newItem.placeId

            override fun areContentsTheSame(
                oldItem: RestaurantResult,
                newItem: RestaurantResult
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: RestaurantResult,
                newItem: RestaurantResult
            ) = if (oldItem.isFavorite != newItem.isFavorite) {
                bundleOf(KEY_IS_FAVORITE to newItem.isFavorite)
            } else {
                null
            }
        }
    }
}