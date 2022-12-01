package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ItemPopularViewBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter
import java.io.IOException

class PopularSearchAdapter : BaseRvAdapter<ItemPopularViewBinding, PlaceList>(R.layout.item_popular_view) {

    private val itemCallback = object : DiffUtil.ItemCallback<PlaceList>() {
        // 比對新舊 Item
        override fun areItemsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem.uid == newItem.uid
        }
        // 比對新舊 Item 內容
        override fun areContentsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((String) -> Unit)

    fun setterData(placeList: ArrayList<PlaceList>) {
        differ.submitList(placeList)
        initData(differ.currentList)
    }

    fun getterData(position: Int): PlaceList = differ.currentList[position]

    override fun bind(binding: ItemPopularViewBinding, item: PlaceList, position: Int) {
        super.bind(binding, item, position)
        try {
            binding.run {
                photoReference = if (item.photos != null && item.photos.size > 0)
                    item.photos[0].photo_reference
                else
                    ""
                tvTitle.text = item.name
                tvRating.text = item.rating.star.toString()
                rating.rating = item.rating.star
                tvRatingTotal.text = String.format("(%s)", item.rating.total)
                tvVicinity.text = item.address
            }
            binding.root.setOnClickListener { onItemClick.invoke(item.uid) }
        } catch (ignored: IOException) {
        }
    }
}