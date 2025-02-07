package mai.project.foodmap.features.home_features.profilesTabScreen.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.foodmap.databinding.ItemSettingsLabelTermBinding

class SettingsLabelTermAdapter(
    private val terms: List<Model>
) : RecyclerView.Adapter<ViewHolder>() {

    data class Model(
        val id: String,
        val name: String,
        val subName: String = ""
    )

    var onItemClick: ((Model) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        SettingsLabelTermViewHolder.from(parent)

    override fun getItemCount(): Int = terms.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is SettingsLabelTermViewHolder) holder.bind(terms[position], onItemClick)
    }

    private class SettingsLabelTermViewHolder(
        private val binding: ItemSettingsLabelTermBinding
    ) : ViewHolder(binding.root) {

        fun bind(
            item: Model,
            onItemClick: ((Model) -> Unit)?
        ) = with(binding) {
            tvName.text = item.name
            tvSubName.text = item.subName
            tvSubName.isVisible = item.subName.isNotEmpty()

            root.onClick { onItemClick?.invoke(item) }
        }

        companion object {
            fun from(parent: ViewGroup) = SettingsLabelTermViewHolder(
                parent.inflateBinding(ItemSettingsLabelTermBinding::inflate)
            )
        }
    }
}