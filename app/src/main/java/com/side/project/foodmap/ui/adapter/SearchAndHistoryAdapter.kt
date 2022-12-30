package com.side.project.foodmap.ui.adapter

import android.view.View
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.HistorySearch
import com.side.project.foodmap.databinding.ItemSearchBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class SearchAndHistoryAdapter : BaseRvListAdapter<ItemSearchBinding, HistorySearch>(R.layout.item_search) {

    lateinit var onItemClick: ((HistorySearch) -> Unit)
    lateinit var onItemLongClick: ((View, HistorySearch) -> Unit)

    override fun bind(item: HistorySearch, binding: ItemSearchBinding, position: Int) {
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

            root.setOnClickListener { onItemClick.invoke(item) }
            root.setOnLongClickListener {
                if (!item.isSearch)
                    onItemLongClick.invoke(root, item)
                true
            }
        }
    }
}