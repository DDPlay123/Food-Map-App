package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ItemMapRestaurantViewBinding
import java.io.IOException

class MapRestaurantAdapter : RecyclerView.Adapter<MapRestaurantAdapter.ViewHolder>() {

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

    fun setData(placeList: ArrayList<PlaceList>) = differ.submitList(placeList)

    fun getData(position: Int): PlaceList = differ.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemMapRestaurantViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            try {
                binding.data = getData(absoluteAdapterPosition)
                binding.photoReference = if (getData(absoluteAdapterPosition).photos != null && (getData(absoluteAdapterPosition).photos?.size ?: 0) > 0)
                    getData(absoluteAdapterPosition).photos?.get(0)
                else
                    ""

                binding.root.setOnClickListener { onItemClick.invoke(getData(absoluteAdapterPosition).place_id) }
            } catch (ignored: IOException) {
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemMapRestaurantViewBinding) : RecyclerView.ViewHolder(binding.root)
}