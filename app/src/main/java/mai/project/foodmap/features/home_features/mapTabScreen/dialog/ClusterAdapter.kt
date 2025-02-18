package mai.project.foodmap.features.home_features.mapTabScreen.dialog

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.transform.RoundedCornersTransformation
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.inflateBinding
import mai.project.core.utils.ImageLoaderUtil
import mai.project.foodmap.databinding.ItemClusterListBinding
import mai.project.foodmap.features.home_features.mapTabScreen.utils.RestaurantClusterItem

class ClusterAdapter : RecyclerView.Adapter<ViewHolder>() {

    private val items = mutableListOf<RestaurantClusterItem>()

    var onItemClick: ((RestaurantClusterItem) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<RestaurantClusterItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ClusterViewHolder.from(parent)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ClusterViewHolder) holder.bind(items[position], onItemClick)
    }

    private class ClusterViewHolder(
        private val binding: ItemClusterListBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: RestaurantClusterItem,
            onItemClick: ((RestaurantClusterItem) -> Unit)?
        ) = with(binding) {
            ImageLoaderUtil.loadImage(
                imageView = imgPhoto,
                resource = item.data.photos.firstOrNull().orEmpty(),
                imageType = ImageType.DEFAULT,
                transformation = RoundedCornersTransformation(25f)
            )
            tvName.text = item.title

            root.setOnClickListener { onItemClick?.invoke(item) }
        }

        companion object {
            fun from(parent: ViewGroup) = ClusterViewHolder(
                parent.inflateBinding(ItemClusterListBinding::inflate)
            )
        }
    }
}