package com.side.project.foodmap.ui.adapter

import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.Review
import com.side.project.foodmap.databinding.ItemGoogleReviewsBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class GoogleReviewsAdapter : BaseRvListAdapter<ItemGoogleReviewsBinding, Review>(R.layout.item_google_reviews) {

    lateinit var onItemClick: ((Review) -> Unit)

    override fun bind(item: Review, binding: ItemGoogleReviewsBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            reviews = item
            imgPicture.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}