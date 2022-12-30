package com.side.project.foodmap.ui.adapter

import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemPromptSelectBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class RegionSelectAdapter : BaseRvListAdapter<ItemPromptSelectBinding, String>(R.layout.item_prompt_select) {
    private var selectPosition = -1

    lateinit var onItemClick: ((String) -> Unit)

    fun setSelectPosition(position: Int) { selectPosition = position }

    override fun bind(item: String, binding: ItemPromptSelectBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            // initialize
            isFinal = position == currentList.size - 1
            itemName = item
            isCheck = selectPosition == position
            // listener
            root.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}