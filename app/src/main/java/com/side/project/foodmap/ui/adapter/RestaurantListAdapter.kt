package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ItemRestaurantViewBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.util.tools.Method
import java.io.IOException

class RestaurantListAdapter : RecyclerView.Adapter<RestaurantListAdapter.ViewHolder>() {

    private val itemCallback = object : DiffUtil.ItemCallback<PlaceList>() {
        // 比對新舊 Item
        override fun areItemsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem.place_id == newItem.place_id
        }

        // 比對新舊 Item 內容
        override fun areContentsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((String) -> Unit)
    lateinit var onItemFavoriteClick: ((String, Boolean) -> Boolean)

    fun setData(placeList: List<PlaceList>) = differ.submitList(placeList)

    fun getData(position: Int): PlaceList = differ.currentList[position]

    private lateinit var myLocation: LatLng
    fun setMyLocation(startLatLng: LatLng) {
        myLocation = startLatLng
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRestaurantViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val params = binding.cardView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.cardView.layoutParams = params
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.apply {
                binding.executePendingBindings() // 即時更新
                binding.data = getData(absoluteAdapterPosition)
                binding.isFavorite = getData(absoluteAdapterPosition).isFavorite
                binding.photoReference = if (getData(absoluteAdapterPosition).photos != null && (getData(absoluteAdapterPosition).photos?.size ?: 0) > 0)
                    getData(absoluteAdapterPosition).photos?.get(0)
                else
                    ""

                if (::myLocation.isInitialized && (myLocation.latitude != 0.0 || myLocation.longitude != 0.0)) {
                    binding.distance = Method.getDistance(myLocation, LatLng(getData(absoluteAdapterPosition).location.lat, getData(absoluteAdapterPosition).location.lng))
                    binding.tvDistance.display()
                } else
                    binding.tvDistance.gone()

                binding.root.setOnClickListener { onItemClick.invoke(getData(absoluteAdapterPosition).place_id) }
                binding.imgFavorite.setOnClickListener {
                    binding.isFavorite = onItemFavoriteClick.invoke(getData(absoluteAdapterPosition).place_id, getData(absoluteAdapterPosition).isFavorite)
                }
            }
        } catch (ignored: IOException) {
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemRestaurantViewBinding) : RecyclerView.ViewHolder(binding.root)
}