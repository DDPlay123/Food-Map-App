package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.google.placesSearch.Result
import com.side.project.foodmap.databinding.ItemPopularViewBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter
import java.io.IOException

class PopularSearchAdapter : BaseRvAdapter<ItemPopularViewBinding, Result>(R.layout.item_popular_view) {

    private val itemCallback = object : DiffUtil.ItemCallback<Result>() {
        // 比對新舊 Item
        override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem.place_id == newItem.place_id
        }
        // 比對新舊 Item 內容
        override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    fun setterData(placesSearchResult: List<Result>) {
        differ.submitList(placesSearchResult)
        initData(differ.currentList)
    }

    fun getterData(position: Int): Result = differ.currentList[position]

    override fun bind(binding: ItemPopularViewBinding, item: Result, position: Int) {
        super.bind(binding, item, position)
        try {
            binding.run {
                photoReference = item.photos?.get(0)?.photo_reference ?: ""
                tvTitle.text = item.name ?: ""
                tvRating.text = (item.rating ?: 0F).toString()
                rating.rating = item.rating ?: 0F
                tvRatingTotal.text = "(${item.user_ratings_total ?: "0"})"
                tvVicinity.text = item.vicinity ?: ""
            }
        } catch (ignored: IOException) {
        }
    }
}