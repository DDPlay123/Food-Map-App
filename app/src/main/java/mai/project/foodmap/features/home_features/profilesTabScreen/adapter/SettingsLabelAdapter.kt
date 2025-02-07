package mai.project.foodmap.features.home_features.profilesTabScreen.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.extensions.inflateBinding
import mai.project.foodmap.databinding.ItemSettingsLabelBinding

class SettingsLabelAdapter : ListAdapter<SettingsLabelAdapter.Model, ViewHolder>(DiffUtilCallback) {

    data class Model(
        val title: String,
        val terms: List<SettingsLabelTermAdapter.Model>
    )

    var onItemClick: ((SettingsLabelTermAdapter.Model) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        SettingsLabelViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is SettingsLabelViewHolder) holder.bind(getItem(position), onItemClick)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is SettingsLabelViewHolder) holder.release()
    }

    private class SettingsLabelViewHolder(
        private val binding: ItemSettingsLabelBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: Model,
            onItemClick: ((SettingsLabelTermAdapter.Model) -> Unit)?
        ) = with(binding) {
            tvTitle.text = item.title
            val adapter = SettingsLabelTermAdapter(item.terms).apply {
                this.onItemClick = onItemClick
            }
            rvTerms.adapter = adapter
        }

        fun release() {
            binding.rvTerms.adapter = null
        }

        companion object {
            fun from(parent: ViewGroup) = SettingsLabelViewHolder(
                parent.inflateBinding(ItemSettingsLabelBinding::inflate)
            )
        }
    }

    companion object {
        private val DiffUtilCallback = object : DiffUtil.ItemCallback<Model>() {
            override fun areItemsTheSame(
                oldItem: Model,
                newItem: Model,
            ): Boolean = oldItem.title == newItem.title

            override fun areContentsTheSame(
                oldItem: Model,
                newItem: Model
            ): Boolean = oldItem == newItem
        }
    }
}