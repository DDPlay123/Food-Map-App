package com.side.project.foodmap.ui.adapter

import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemFavoritePhotoBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class FavoritePhotosListAdapter : BaseRvListAdapter<ItemFavoritePhotoBinding, String>(R.layout.item_favorite_photo) {

    lateinit var onItemClick: ((List<String>, Int) -> Unit)

    override fun bind(item: String, binding: ItemFavoritePhotoBinding, position: Int) {
        super.bind(item, binding, position)
        binding.run {
            photoReference = item
            imgPhoto.transitionName = item
            root.setOnClickListener { onItemClick.invoke(currentList, position) }
        }
    }
}