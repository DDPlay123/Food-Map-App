package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.PlaceList
import com.side.project.foodmap.databinding.ItemMapRestaurantViewBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class MapRestaurantAdapter :
    BaseRvListAdapter<ItemMapRestaurantViewBinding, PlaceList>(R.layout.item_map_restaurant_view, ItemCallback()) {

    class ItemCallback : DiffUtil.ItemCallback<PlaceList>() {
        override fun areItemsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem.place_id == newItem.place_id
        }

        override fun areContentsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem == newItem
        }
    }

    lateinit var onItemClick: ((PlaceList) -> Unit)

    override fun bind(item: PlaceList, binding: ItemMapRestaurantViewBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            data = item
            photoReference = if (item.photos.isNotEmpty())
                item.photos[0]
            else
                ""

            root.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}