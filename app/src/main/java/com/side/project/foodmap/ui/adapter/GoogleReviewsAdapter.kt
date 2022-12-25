package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.Review
import com.side.project.foodmap.databinding.ItemGoogleReviewsBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class GoogleReviewsAdapter : BaseRvAdapter<ItemGoogleReviewsBinding, Review>(R.layout.item_google_reviews) {

    lateinit var onItemClick: ((Review) -> Unit)

    @SuppressLint("NotifyDataSetChanged")
    fun setReviewList(reviewsList: List<Review>) {
        initData(reviewsList)
        notifyDataSetChanged()
    }

    override fun bind(binding: ItemGoogleReviewsBinding, item: Review, position: Int) {
        super.bind(binding, item, position)
        binding.reviews = item
        binding.imgPicture.setOnClickListener { onItemClick.invoke(item) }
    }
}