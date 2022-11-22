package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemPromptSelectBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class RegionSelectAdapter : BaseRvAdapter<ItemPromptSelectBinding, String>(R.layout.item_prompt_select) {
    private var listSize = 0
    private var selectPosition = -1

    lateinit var onItemClick: ((String) -> Unit)

    @SuppressLint("NotifyDataSetChanged")
    fun setRegionList(regionList: ArrayList<String>, position: Int) {
        listSize = regionList.size
        selectPosition = position
        initData(regionList)
        notifyDataSetChanged()
    }

    override fun bind(binding: ItemPromptSelectBinding, item: String, position: Int) {
        super.bind(binding, item, position)
        binding.run {
            // initialize
            isFinal = position == listSize - 1
            itemName = item
            isCheck = selectPosition == position
            // listener
            root.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
}