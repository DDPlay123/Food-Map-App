package mai.project.core.widget.recyclerView_adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.widget.DownToCloseImageView

/**
 * 圖片預覽的 adapter
 *
 * - 使用 [DownToCloseImageView]
 */
class ImagePreviewPagerAdapter: ListAdapter<Any, ViewHolder>(DiffUtilCallback) {

    var onClosed: ((Any) -> Unit)? = null

    private var closedPositions = mutableListOf<Int>()

    fun resetState() {
        closedPositions.forEach {
            notifyItemChanged(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ImagePreviewViewHolder.create(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ImagePreviewViewHolder) holder.bind(
            item = getItem(position),
            onClosed = {
                closedPositions.add(position)
                onClosed?.invoke(it)
            }
        )
    }

    private class ImagePreviewViewHolder(
        private val imageView: DownToCloseImageView
    ): ViewHolder(imageView) {

        fun bind(
            item: Any,
            onClosed: ((Any) -> Unit)?
        ) = with(imageView) {
            setImage(item)
            onCloseListener = { onClosed?.invoke(item) }
        }

        companion object {
            fun create(parent: ViewGroup): ImagePreviewViewHolder {
                val imageView = DownToCloseImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                return ImagePreviewViewHolder(imageView)
            }
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = true
        }
    }
}
