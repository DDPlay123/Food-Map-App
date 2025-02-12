package mai.project.foodmap.features.restaurant_feature.restaurantDetailScreen

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.transform.RoundedCornersTransformation
import mai.project.core.annotations.ImageType
import mai.project.core.utils.ImageLoaderUtil

class PhotosAdapter : ListAdapter<String, ViewHolder>(DiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        PhotosViewHolder.create(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is PhotosViewHolder) holder.bind(getItem(position))
    }
    
    private class PhotosViewHolder(
        private val imageView: ImageView
    ) : ViewHolder(imageView) {
        
        fun bind(
            item: String
        ) {
            ImageLoaderUtil.loadImage(
                imageView = imageView,
                resource = item,
                imageType = ImageType.DEFAULT,
                transformation = RoundedCornersTransformation(25f)
            )
        }
        
        companion object {
            fun create(parent: ViewGroup): PhotosViewHolder {
                val imageView = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.FIT_XY
                }
                return PhotosViewHolder(imageView)
            }
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean = oldItem == newItem
        }
    }
}