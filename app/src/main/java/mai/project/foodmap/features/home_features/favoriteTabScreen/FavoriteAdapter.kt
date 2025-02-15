package mai.project.foodmap.features.home_features.favoriteTabScreen

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.databinding.ItemMyFavoriteBinding
import mai.project.foodmap.domain.models.MyFavoriteResult
import java.util.Locale

class FavoriteAdapter : ListAdapter<MyFavoriteResult, ViewHolder>(DiffUtilCallback) {

    var onItemClick: ((MyFavoriteResult) -> Unit)? = null
    var onPhotoClick: ((List<String>, String) -> Unit)? = null
    var onAddressClick: ((MyFavoriteResult) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        FavoriteViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is FavoriteViewHolder) holder.bind(
            item = getItem(position),
            onItemClick = onItemClick,
            onPhotoClick = onPhotoClick,
            onAddressClick = onAddressClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty() && holder is FavoriteViewHolder) {
            holder.bind(
                item = getItem(position),
                onItemClick = onItemClick,
                onPhotoClick = onPhotoClick,
                onAddressClick = onAddressClick
            )
        } else {
            for (payload in payloads) {
                if (payload is Bundle &&
                    payload.containsKey(KEY_IS_FAVORITE) &&
                    holder is FavoriteViewHolder
                ) {
                    val isFavorite = payload.getBoolean(KEY_IS_FAVORITE)
                    holder.updateFavorite(isFavorite)
                }
            }
        }
    }

    private class FavoriteViewHolder(
        private val binding: ItemMyFavoriteBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: MyFavoriteResult,
            onItemClick: ((MyFavoriteResult) -> Unit)?,
            onPhotoClick: ((List<String>, String) -> Unit)?,
            onAddressClick: ((MyFavoriteResult) -> Unit)?
        ) = with(binding) {
            tvName.text = item.name
            tvRating.text = "${item.ratingStar}"
            rating.rating = item.ratingStar
            tvRatingTotal.text = String.format(Locale.getDefault(), "(%d)", item.ratingTotal)
            tvAddress.text = item.address

            rvPhotos.isVisible = item.photos.isNotEmpty()

            if (rvPhotos.itemDecorationCount <= 0) {
                rvPhotos.addItemDecoration(
                    SpacesItemDecoration(
                        direction = Direction.HORIZONTAL,
                        space = 10.DP,
                        sideSpace = 10.DP
                    )
                )
            }

            (rvPhotos.adapter as? FavoritePhotoAdapter)?.let {
                it.setPhotos(item.photos)
                it.onPhotoClick = { p -> onPhotoClick?.invoke(item.photos, p) }
            } ?: FavoritePhotoAdapter().also {
                rvPhotos.adapter = it
                it.setPhotos(item.photos)
                it.onPhotoClick = { p -> onPhotoClick?.invoke(item.photos, p) }
            }

            clRoot.onClick { onItemClick?.invoke(item) }
            tvAddress.onClick { onAddressClick?.invoke(item) }

            // 收藏, 網站, 致電, 分享, Google Map
        }

        fun updateFavorite(isFavorite: Boolean) = with(binding) {

        }

        companion object {
            fun from(parent: ViewGroup) = FavoriteViewHolder(
                parent.inflateBinding(ItemMyFavoriteBinding::inflate)
            )
        }
    }

    companion object {
        private const val KEY_IS_FAVORITE = "KEY_IS_FAVORITE"

        private val DiffUtilCallback = object : DiffUtil.ItemCallback<MyFavoriteResult>() {
            override fun areItemsTheSame(
                oldItem: MyFavoriteResult,
                newItem: MyFavoriteResult
            ): Boolean = oldItem.placeId == newItem.placeId

            override fun areContentsTheSame(
                oldItem: MyFavoriteResult,
                newItem: MyFavoriteResult
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: MyFavoriteResult,
                newItem: MyFavoriteResult
            ) = if (oldItem.isFavorite != newItem.isFavorite) {
                bundleOf(KEY_IS_FAVORITE to newItem.isFavorite)
            } else {
                null
            }
        }
    }
}