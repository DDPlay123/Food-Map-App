package com.side.project.foodmap.ui.adapter

import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ItemRestaurantViewBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter
import com.side.project.foodmap.util.tools.Method

class RestaurantListAdapter :
    BaseRvListAdapter<ItemRestaurantViewBinding, PlaceList>(R.layout.item_restaurant_view, ItemCallback()), Filterable {

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
    fun setMyLocation(startLatLng: LatLng) {
        myLocation = startLatLng
    }

    override fun initialize(binding: ItemRestaurantViewBinding) {
        super.initialize(binding)
        val params = binding.cardView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.cardView.layoutParams = params
    }

    override fun bind(item: PlaceList, binding: ItemRestaurantViewBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            executePendingBindings() // 即時更新
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

    override fun getFilter(): Filter = customFilter

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList = mutableListOf<PlaceList>()
            val filterPattern = constraint.toString().lowercase().trim()
            if (constraint.isEmpty() || filterPattern == "")
                filteredList.addAll(currentList)
            else
                currentList.forEach { item ->
                    if (item.name.lowercase().trim().contains(filterPattern))
                        filteredList.add(item)
                }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, filterResults: FilterResults) {
            submitList(filterResults.values as MutableList<PlaceList>)
        }
    }
}