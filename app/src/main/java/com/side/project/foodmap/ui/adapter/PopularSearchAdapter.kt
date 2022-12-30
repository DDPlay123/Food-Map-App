package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ItemRestaurantViewBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter
import com.side.project.foodmap.util.tools.Method

class PopularSearchAdapter :
    BaseRvListAdapter<ItemRestaurantViewBinding, PlaceList>(R.layout.item_restaurant_view, ItemCallback()) {

    class ItemCallback : DiffUtil.ItemCallback<PlaceList>() {
        override fun areItemsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem.place_id == newItem.place_id
        }

        override fun areContentsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem == newItem
        }
    }

    lateinit var onItemClick: ((String) -> Unit)
    lateinit var onItemFavoriteClick: ((String, Boolean) -> Boolean)

    private lateinit var myLocation: LatLng
    fun setMyLocation(startLatLng: LatLng) { myLocation = startLatLng }

    override fun bind(item: PlaceList, binding: ItemRestaurantViewBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            data = item
            isFavorite = item.isFavorite
            photoReference = if (item.photos != null && item.photos.isNotEmpty())
                item.photos[0]
            else
                ""

            if (::myLocation.isInitialized && (myLocation.latitude != 0.0 || myLocation.longitude != 0.0)) {
                distance = Method.getDistance(myLocation, LatLng(item.location.lat, item.location.lng))
                tvDistance.display()
            } else
                tvDistance.gone()

            root.setOnClickListener { onItemClick.invoke(item.place_id) }
            imgFavorite.setOnClickListener { isFavorite = onItemFavoriteClick.invoke(item.place_id, item.isFavorite) }
        }
    }
}