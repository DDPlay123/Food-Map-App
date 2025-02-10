package mai.project.foodmap.features.home_features.homeTabScreen

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.transform.RoundedCornersTransformation
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.inflateBinding
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.R
import mai.project.foodmap.databinding.ItemDrawCardBinding
import mai.project.foodmap.domain.models.RestaurantResult
import java.util.Locale

class DrawCardAdapter(
    private val imageLoaderUtil: ImageLoaderUtil
) : ListAdapter<RestaurantResult, ViewHolder>(DiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        DrawCardViewModel.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is DrawCardViewModel) holder.bind(getItem(position), imageLoaderUtil)
    }

    private class DrawCardViewModel(
        private val binding: ItemDrawCardBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: RestaurantResult,
            imageLoaderUtil: ImageLoaderUtil
        ) = with(binding) {
            val context = root.context
            imageLoaderUtil.loadImage(
                imageView = imgPhoto,
                resource = item.photos.firstOrNull().orEmpty(),
                imageType = ImageType.DEFAULT,
                transformation = RoundedCornersTransformation(25f)
            )
            tvName.text = item.name
            tvRating.text = item.ratingStar.toString()
            rating.rating = item.ratingStar
            tvRatingTotal.text = String.format(Locale.getDefault(), "(%d)", item.ratingTotal)
            tvAddress.text = item.address
            if (item.isFavorite) {
                imgPhoto.setImageResource(R.drawable.vector_favorite)
            } else {
                imgPhoto.setImageResource(R.drawable.vector_favorite_border)
            }
            tvDistance.text = String.format(
                Locale.getDefault(),
                if (item.distance < 1000) context.getString(R.string.format_number_meter) else context.getString(R.string.format_number_kilometer),
                if (item.distance < 1000) item.distance else item.distance / 1000
            )
        }

        companion object {
            fun from(parent: ViewGroup) = DrawCardViewModel(
                parent.inflateBinding(ItemDrawCardBinding::inflate)
            )
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<RestaurantResult>() {
            override fun areItemsTheSame(
                oldItem: RestaurantResult,
                newItem: RestaurantResult
            ): Boolean = oldItem.placeId == newItem.placeId

            override fun areContentsTheSame(
                oldItem: RestaurantResult,
                newItem: RestaurantResult
            ): Boolean = oldItem == newItem
        }
    }
}