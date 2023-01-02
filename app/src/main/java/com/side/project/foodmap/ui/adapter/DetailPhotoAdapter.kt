package com.side.project.foodmap.ui.adapter

import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemSliderPhotoBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class DetailPhotoAdapter : BaseRvListAdapter<ItemSliderPhotoBinding, String>(R.layout.item_slider_photo) {

    lateinit var onItemClick: ((Int) -> Unit)

    override fun bind(item: String, binding: ItemSliderPhotoBinding, position: Int) {
        super.bind(item, binding, position)
        binding.run {
            photoReference = item
            imgPicture.transitionName = item
            root.setOnClickListener { onItemClick.invoke(position) }
        }
    }
}