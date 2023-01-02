package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.PlaceList
import com.side.project.foodmap.databinding.ItemRestaurantViewBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter
import com.side.project.foodmap.util.tools.Method

class PopularSearchAdapter :
    BaseRvListAdapter<ItemRestaurantViewBinding, PlaceList>(R.layout.item_restaurant_view, ItemCallback()) {

    class ItemCallback : DiffUtil.ItemCallback<PlaceList>() {
        override fun areItemsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem.place_id == newItem.place_id && oldItem.isFavorite == newItem.isFavorite
        }

        override fun areContentsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem == newItem
        }
    }

    override fun submitList(list: MutableList<PlaceList>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    lateinit var onItemClick: ((String) -> Unit)
    lateinit var onItemFavoriteClick: ((String, Boolean) -> Boolean)

    private lateinit var myLocation: Location
    fun setMyLocation(startLatLng: Location) { myLocation = startLatLng }

    override fun bind(item: PlaceList, binding: ItemRestaurantViewBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            executePendingBindings()
            var mIsFavorite = item.isFavorite
            data = item
            isFavorite = item.isFavorite
            photoReference = if (item.photos.isNotEmpty())
                item.photos[0]
            else
                ""

            if (::myLocation.isInitialized && (myLocation.lat != 0.0 || myLocation.lng != 0.0)) {
                distance = Method.getDistance(myLocation, Location(item.location.lat, item.location.lng))
                tvDistance.display()
            } else
                tvDistance.gone()

            root.setOnClickListener { onItemClick.invoke(item.place_id) }
            imgFavorite.setOnClickListener {
                isFavorite = onItemFavoriteClick.invoke(item.place_id, mIsFavorite)
                mIsFavorite = onItemFavoriteClick.invoke(item.place_id, mIsFavorite)
            }
        }
    }
}