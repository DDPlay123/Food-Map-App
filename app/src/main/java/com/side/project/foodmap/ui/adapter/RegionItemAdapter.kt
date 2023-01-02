package com.side.project.foodmap.ui.adapter

import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.AutoComplete
import com.side.project.foodmap.databinding.ItemSearchBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class RegionItemAdapter : BaseRvListAdapter<ItemSearchBinding, AutoComplete>(R.layout.item_search) {

    lateinit var onItemClick: ((AutoComplete) -> Unit)

    override fun bind(item: AutoComplete, binding: ItemSearchBinding, position: Int) {
        super.bind(item, binding, position)
        binding.run {
            imgIcon.setImageResource(R.drawable.ic_location)
            name = item.name
            address = item.address
            layoutSwipe.setLockDrag(true)
            layoutBody.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}