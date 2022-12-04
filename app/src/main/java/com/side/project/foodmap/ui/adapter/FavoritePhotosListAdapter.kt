package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemFavoritePhotoBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class FavoritePhotosListAdapter : BaseRvAdapter<ItemFavoritePhotoBinding, String>(R.layout.item_favorite_photo) {

    @SuppressLint("NotifyDataSetChanged")
    fun setPhotosList(photo: List<String>) {
        initData(photo)
        notifyDataSetChanged()
    }

    override fun bind(binding: ItemFavoritePhotoBinding, item: String, position: Int) {
        super.bind(binding, item, position)
        binding.photoReference = item
    }
}