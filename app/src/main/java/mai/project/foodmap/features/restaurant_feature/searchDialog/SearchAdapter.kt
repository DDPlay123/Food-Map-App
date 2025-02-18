package mai.project.foodmap.features.restaurant_feature.searchDialog

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.extensions.getDrawableCompat
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.foodmap.R
import mai.project.foodmap.databinding.ItemSearchOrRecordBinding
import mai.project.foodmap.domain.models.SearchRestaurantResult

class SearchAdapter : ListAdapter<SearchRestaurantResult, ViewHolder>(DiffUtilCallback) {

    var onKeywordItemClick: ((SearchRestaurantResult) -> Unit)? = null
    var onRestaurantItemClick: ((SearchRestaurantResult) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isSearch) SEARCH else HISTORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == HISTORY) {
            HistoryViewHolder.from(parent)
        } else {
            SearchViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is BaseViewHolder) holder.bind(
            item = getItem(position),
            onKeywordItemClick = onKeywordItemClick,
            onRestaurantItemClick = onRestaurantItemClick
        )
    }

    private abstract class BaseViewHolder(
        private val binding: ItemSearchOrRecordBinding
    ) : ViewHolder(binding.root) {
        fun bind(
            item: SearchRestaurantResult,
            onKeywordItemClick: ((SearchRestaurantResult) -> Unit)?,
            onRestaurantItemClick: ((SearchRestaurantResult) -> Unit)?
        ) = with(binding) {
            val context = root.context
            tvName.text = item.name
            tvAddress.text = item.address
            tvAddress.isVisible = item.address.isNotEmpty()

            when {
                !item.isSearch -> {
                    imgIcon.setBackgroundDrawable(
                        context.getDrawableCompat(R.drawable.bg_item_search_icon)
                    )
                    imgIcon.setImageResource(R.drawable.vector_history)
                }

                item.placeId.isNotEmpty() -> {
                    imgIcon.setBackgroundDrawable(
                        context.getDrawableCompat(R.drawable.bg_item_search_icon)
                    )
                    imgIcon.setImageResource(R.drawable.vector_restaurant_menu)
                }

                else -> {
                    imgIcon.setBackgroundDrawable(
                        context.getDrawableCompat(R.drawable.bg_item_search_keyword_icon)
                    )
                    imgIcon.setImageResource(R.drawable.vector_search)
                }
            }

            root.onClick {
                if (item.placeId.isNotEmpty()) {
                    onRestaurantItemClick?.invoke(item)
                } else {
                    onKeywordItemClick?.invoke(item)
                }
            }
        }
    }

    private class HistoryViewHolder(
        binding: ItemSearchOrRecordBinding
    ) : BaseViewHolder(binding) {
        companion object {
            fun from(parent: ViewGroup) = HistoryViewHolder(
                parent.inflateBinding(ItemSearchOrRecordBinding::inflate)
            )
        }
    }

    private class SearchViewHolder(
        binding: ItemSearchOrRecordBinding
    ) : BaseViewHolder(binding) {
        companion object {
            fun from(parent: ViewGroup) = SearchViewHolder(
                parent.inflateBinding(ItemSearchOrRecordBinding::inflate)
            )
        }
    }

    companion object {
        const val HISTORY = 0
        const val SEARCH = 1

        private val DiffUtilCallback = object : DiffUtil.ItemCallback<SearchRestaurantResult>() {
            override fun areItemsTheSame(
                oldItem: SearchRestaurantResult,
                newItem: SearchRestaurantResult
            ): Boolean = oldItem.placeId == newItem.placeId

            override fun areContentsTheSame(
                oldItem: SearchRestaurantResult,
                newItem: SearchRestaurantResult
            ): Boolean = oldItem == newItem
        }
    }
}