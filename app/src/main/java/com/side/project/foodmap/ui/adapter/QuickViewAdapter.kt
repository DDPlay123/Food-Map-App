package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.google.placesSearch.Result
import com.side.project.foodmap.databinding.ItemQuickViewBinding

class QuickViewAdapter : RecyclerView.Adapter<QuickViewAdapter.ViewHolder>() {
    private var placesSearchResult = ArrayList<Result>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(placesSearchResult: ArrayList<Result>) {
        this.placesSearchResult = placesSearchResult
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemQuickViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = placesSearchResult[position]
        holder.binding.run {
            photoReference = item.photos[0].photo_reference
            tvTitle.text = item.name
            tvRating.text = item.rating.toString()
            rating.rating = item.rating
            tvRatingTotal.text = "(${item.user_ratings_total})"
            tvVicinity.text = item.vicinity
            isOpen = item.opening_hours?.open_now ?: false
        }
    }

    override fun getItemCount(): Int = placesSearchResult.size

    class ViewHolder(val binding: ItemQuickViewBinding): RecyclerView.ViewHolder(binding.root)
}