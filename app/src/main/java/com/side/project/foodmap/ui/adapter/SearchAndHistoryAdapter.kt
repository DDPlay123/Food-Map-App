package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.HistorySearch
import com.side.project.foodmap.databinding.ItemSearchBinding

class SearchAndHistoryAdapter : RecyclerView.Adapter<SearchAndHistoryAdapter.ViewHolder>() {
    private lateinit var keyword: String
    private var isHistory: Boolean = false

    private val itemCallback = object : DiffUtil.ItemCallback<HistorySearch>() {

        override fun areItemsTheSame(oldItem: HistorySearch, newItem: HistorySearch): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: HistorySearch, newItem: HistorySearch): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((HistorySearch) -> Unit)

    fun setData(isHistory: Boolean, keyword: String, historySearchList: List<HistorySearch>) {
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

        differ.submitList(mPredictionList)
    }

    fun getData(position: Int): HistorySearch = differ.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            isFirst = position == 0
            isHistory = this@SearchAndHistoryAdapter.isHistory
            name = getData(holder.adapterPosition).name
            address = getData(holder.adapterPosition).address

            if (this@SearchAndHistoryAdapter.isHistory)
                imgIcon.setImageResource(R.drawable.ic_history)
            else {
                if (position == 0)
                    imgIcon.setImageResource(R.drawable.ic_baseline_search)
                else
                    imgIcon.setImageResource(R.drawable.ic_location)
            }

            root.setOnClickListener { onItemClick.invoke(getData(holder.adapterPosition)) }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)
}