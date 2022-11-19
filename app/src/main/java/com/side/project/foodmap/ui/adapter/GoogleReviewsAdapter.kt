package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.google.placesDetails.Review
import com.side.project.foodmap.databinding.ItemGoogleReviewsBinding

class GoogleReviewsAdapter : RecyclerView.Adapter<GoogleReviewsAdapter.ViewHolder>() {

    private val itemCallback = object : DiffUtil.ItemCallback<Review>() {
        // 比對新舊 Item
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.author_name == newItem.author_name
        }
        // 比對新舊 Item 內容
        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((Review) -> Unit)

    fun setData(reviewsList: List<Review>) = differ.submitList(reviewsList)

    fun getData(position: Int): Review = differ.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemGoogleReviewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.reviews = differ.currentList[position]
        holder.binding.imgPicture.setOnClickListener { onItemClick.invoke(getData(position)) }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemGoogleReviewsBinding): RecyclerView.ViewHolder(binding.root)
}