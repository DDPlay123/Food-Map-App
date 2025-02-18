package mai.project.foodmap.features.myPlace_feature.addPlaceScreen

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.foodmap.databinding.ItemSearchPlaceBinding
import mai.project.foodmap.domain.models.SearchPlaceResult
import java.util.Locale

class SearchPlaceAdapter : ListAdapter<SearchPlaceResult, ViewHolder>(DiffUtilCallback) {

    var onItemClick: ((SearchPlaceResult) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        SearchPlaceViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is SearchPlaceViewHolder) holder.bind(getItem(position), onItemClick)
    }

    private class SearchPlaceViewHolder(
        private val binding: ItemSearchPlaceBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: SearchPlaceResult,
            onItemClick: ((SearchPlaceResult) -> Unit)?
        ) = with(binding) {
            tvAddress.text = String.format(Locale.getDefault(), "%s\n%s", item.name, item.address)

            clRoot.onClick { onItemClick?.invoke(item) }
        }

        companion object {
            fun from(parent: ViewGroup) = SearchPlaceViewHolder(
                parent.inflateBinding(ItemSearchPlaceBinding::inflate)
            )
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<SearchPlaceResult>() {
            override fun areItemsTheSame(
                oldItem: SearchPlaceResult,
                newItem: SearchPlaceResult
            ): Boolean = oldItem.placeId == newItem.placeId

            override fun areContentsTheSame(
                oldItem: SearchPlaceResult,
                newItem: SearchPlaceResult
            ): Boolean = oldItem == newItem
        }
    }
}