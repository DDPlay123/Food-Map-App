package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ItemRestaurantViewBinding
import java.io.IOException

class RestaurantListAdapter : RecyclerView.Adapter<RestaurantListAdapter.ViewHolder>() {

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

    lateinit var onItemClick: ((String, Boolean) -> Unit)

    fun setData(placeList: List<PlaceList>) = differ.submitList(placeList)

    fun getData(position: Int): PlaceList = differ.currentList[position]

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
                binding.data = getData(adapterPosition)
                binding.photoReference = if (getData(adapterPosition).photos != null && (getData(adapterPosition).photos?.size ?: 0) > 0)
                    getData(adapterPosition).photos?.get(0)?.photo_reference
                else
                    ""
                binding.executePendingBindings() // 即時更新
                binding.root.setOnClickListener { onItemClick.invoke(getData(adapterPosition).uid, getData(adapterPosition).isFavorite) }
            }
        } catch (ignored: IOException) {
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemRestaurantViewBinding) : RecyclerView.ViewHolder(binding.root)
}