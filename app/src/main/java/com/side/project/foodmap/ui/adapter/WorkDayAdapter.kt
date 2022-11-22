package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemPromptSelectBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class WorkDayAdapter : BaseRvAdapter<ItemPromptSelectBinding, String>(R.layout.item_prompt_select) {
    private var listSize = 0

    @SuppressLint("NotifyDataSetChanged")
    fun setWorkdayList(regionList: List<String>) {
        listSize = regionList.size
        initData(regionList)
        notifyDataSetChanged()
    }

    override fun bind(binding: ItemPromptSelectBinding, item: String, position: Int) {
        super.bind(binding, item, position)
        binding.run {
            imgCheck.gone()
            itemName = item
            isFinal = position == listSize - 1
        }
    }
}