package com.side.project.foodmap.ui.adapter

import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.PlaceList
import com.side.project.foodmap.databinding.ItemRestaurantViewBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter
import com.side.project.foodmap.util.tools.Method

class RestaurantListAdapter :
    BaseRvListAdapter<ItemRestaurantViewBinding, PlaceList>(R.layout.item_restaurant_view, ItemCallback()), Filterable {

    class ItemCallback : DiffUtil.ItemCallback<PlaceList>() {
        override fun areItemsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem.place_id == newItem.place_id && oldItem.isFavorite == newItem.isFavorite
        }

        override fun areContentsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem == newItem
        }
    }

    private var placeList = listOf<PlaceList>()
    fun setPlaceList(placeLists: List<PlaceList>) {
        placeList = placeLists
        submitList(placeList.toMutableList())
    }

    lateinit var onItemClick: ((String) -> Unit)
    lateinit var onItemFavoriteClick: ((String, Boolean) -> Boolean)

    private lateinit var myLocation: Location
    fun setMyLocation(startLatLng: Location) { myLocation = startLatLng }

    private var isBlackList: Boolean = false
    fun setIsBlackList(isBlack: Boolean) { isBlackList = isBlack }

    override fun initialize(binding: ItemRestaurantViewBinding) {
        super.initialize(binding)
        val params = binding.cardView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.cardView.layoutParams = params
    }

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

            if (isBlackList)
                imgFavorite.gone()
            else
                imgFavorite.display()

            root.setOnClickListener { onItemClick.invoke(item.place_id) }
            imgFavorite.setOnClickListener {
                isFavorite = onItemFavoriteClick.invoke(item.place_id, mIsFavorite)
                mIsFavorite = onItemFavoriteClick.invoke(item.place_id, mIsFavorite)
            }
        }
    }

    override fun getFilter(): Filter = customFilter

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList = mutableListOf<PlaceList>()
            val filterPattern = constraint.toString().lowercase().trim()
            if (constraint.isEmpty() || filterPattern == "")
                filteredList.addAll(placeList)
            else
                placeList.forEach { item ->
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