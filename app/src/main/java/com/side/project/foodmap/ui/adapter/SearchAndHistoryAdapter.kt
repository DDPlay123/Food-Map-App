package com.side.project.foodmap.ui.adapter

import android.view.View
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.AutoComplete
import com.side.project.foodmap.databinding.ItemSearchBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class SearchAndHistoryAdapter : BaseRvListAdapter<ItemSearchBinding, AutoComplete>(R.layout.item_search) {

    lateinit var onItemClick: ((AutoComplete) -> Unit)
    lateinit var onDeleteClick: ((AutoComplete) -> Unit)

    override fun bind(item: AutoComplete, binding: ItemSearchBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            isFirst = position == 0
            isHistory = !item.isSearch
            name = item.name
            address = item.address

            if (!item.isSearch)
                imgIcon.setImageResource(R.drawable.ic_history)
            else {
                if (position == 0)
                    imgIcon.setImageResource(R.drawable.ic_baseline_search)
                else
                    imgIcon.setImageResource(R.drawable.ic_location)
            }

            layoutSwipe.setLockDrag(item.isSearch)
            layoutBody.setOnClickListener { onItemClick.invoke(item) }
            layoutDelete.setOnClickListener {
                if (!item.isSearch)
                    onDeleteClick.invoke(item)
            }
        }
    }
}