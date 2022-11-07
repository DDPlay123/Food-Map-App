package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.google.placesSearch.PlacesSearch
import com.side.project.foodmap.data.remote.google.placesSearch.Result
import com.side.project.foodmap.databinding.ItemQuickViewBinding
import java.io.IOException

class QuickViewAdapter : RecyclerView.Adapter<QuickViewAdapter.ViewHolder>() {

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


    fun setData(placesSearchResult: List<Result>) = differ.submitList(placesSearchResult)

    fun getData(position: Int): Result = differ.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemQuickViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]
        try {
            holder.binding.run {
                photoReference = item.photos?.get(0)?.photo_reference ?: ""
                tvTitle.text = item.name ?: ""
                tvRating.text = (item.rating ?: 0F).toString()
                rating.rating = item.rating ?: 0F
                tvRatingTotal.text = "(${item.user_ratings_total ?: "0"})"
                tvVicinity.text = item.vicinity ?: ""
                isOpen = item.opening_hours?.open_now ?: false
            }
        } catch (ignored: IOException) {
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemQuickViewBinding): RecyclerView.ViewHolder(binding.root)
}