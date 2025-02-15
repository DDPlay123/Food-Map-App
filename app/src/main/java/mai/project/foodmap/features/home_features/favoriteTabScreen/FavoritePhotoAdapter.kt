package mai.project.foodmap.features.home_features.favoriteTabScreen

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.transform.RoundedCornersTransformation
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.DP
import mai.project.core.extensions.onClick
import mai.project.core.utils.ImageLoaderUtil

class FavoritePhotoAdapter : RecyclerView.Adapter<ViewHolder>() {

    var onPhotoClick: ((String) -> Unit)? = null

    private val photos = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun setPhotos(newPhotos: List<String>) {
        photos.clear()
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        PhotoViewHolder.create(parent)

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is PhotoViewHolder) holder.bind(photos[position], onPhotoClick)
    }

    private class PhotoViewHolder(
        private val imageView: ImageView
    ) : ViewHolder(imageView) {

        fun bind(
            item: String,
            onPhotoClick: ((String) -> Unit)?
        ) = with(imageView) {
            ImageLoaderUtil.loadImage(
                imageView = this,
                resource = item,
                imageType = ImageType.DEFAULT,
                transformation = RoundedCornersTransformation(25f)
            )

            rootView.onClick { onPhotoClick?.invoke(item) }
        }

        companion object {
            fun create(parent: ViewGroup): PhotoViewHolder {
                val imageView = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(150.DP, 150.DP)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return PhotoViewHolder(imageView)
            }
        }
    }
}