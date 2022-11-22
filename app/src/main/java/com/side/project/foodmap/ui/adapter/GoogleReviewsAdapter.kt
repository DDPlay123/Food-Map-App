package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.google.placesDetails.Review
import com.side.project.foodmap.databinding.ItemGoogleReviewsBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class GoogleReviewsAdapter : BaseRvAdapter<ItemGoogleReviewsBinding, Review>(R.layout.item_google_reviews) {

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

    fun setterData(reviewsList: List<Review>) {
        differ.submitList(reviewsList)
        initData(differ.currentList)
    }

    fun getterData(position: Int): Review = differ.currentList[position]

    override fun bind(binding: ItemGoogleReviewsBinding, item: Review, position: Int) {
        super.bind(binding, item, position)
        binding.reviews = differ.currentList[position]
        binding.imgPicture.setOnClickListener { onItemClick.invoke(item) }
    }
}