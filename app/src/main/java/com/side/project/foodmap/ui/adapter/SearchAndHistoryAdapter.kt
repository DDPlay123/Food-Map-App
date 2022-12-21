package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.HistorySearch
import com.side.project.foodmap.data.remote.google.placesAutoComplete.Prediction
import com.side.project.foodmap.data.remote.google.placesAutoComplete.StructuredFormatting
import com.side.project.foodmap.databinding.ItemSearchBinding
import com.side.project.foodmap.helper.getColorCompat
import com.side.project.foodmap.helper.getDrawableCompat
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class SearchAndHistoryAdapter : BaseRvAdapter<ItemSearchBinding, HistorySearch>(R.layout.item_search) {
    private lateinit var keyword: String
    private var isHistory: Boolean = false

    lateinit var onItemClick: ((HistorySearch) -> Unit)

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchList(isHistory: Boolean, keyword: String, historySearchList: List<HistorySearch>) {
        this.isHistory = isHistory
        this.keyword = keyword
        val firstItem = HistorySearch(
            place_id = "",
            name = keyword,
            address = ""
        )
        val mPredictionList = if (isHistory)
            historySearchList
        else
            listOf(firstItem) + historySearchList
        initData(mPredictionList)
        notifyDataSetChanged()
    }

    override fun bind(binding: ItemSearchBinding, item: HistorySearch, position: Int) {
        super.bind(binding, item, position)
        binding.run {
            isFirst = position == 0
            isHistory = this@SearchAndHistoryAdapter.isHistory
            name = item.name
            address = item.address

            if (this@SearchAndHistoryAdapter.isHistory)
                imgIcon.setImageResource(R.drawable.ic_history)
            else {
                if (position == 0)
                    imgIcon.setImageResource(R.drawable.ic_baseline_search)
                else
                    imgIcon.setImageResource(R.drawable.ic_location)
            }

            root.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}