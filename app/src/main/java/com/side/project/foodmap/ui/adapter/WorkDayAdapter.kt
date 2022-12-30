package com.side.project.foodmap.ui.adapter

import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemPromptSelectBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class WorkDayAdapter : BaseRvListAdapter<ItemPromptSelectBinding, String>(R.layout.item_prompt_select) {

    override fun bind(item: String, binding: ItemPromptSelectBinding, position: Int) {
        super.bind(item, binding, position)
        binding.run {
            imgCheck.gone()
            itemName = item
            isFinal = position == currentList.size - 1
        }
    }
}