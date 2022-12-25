package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ItemSliderPhotoBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class DetailPhotoAdapter : BaseRvAdapter<ItemSliderPhotoBinding, String>(R.layout.item_slider_photo) {

    lateinit var onItemClick: ((View, List<String>, String, Int) -> Unit)

    @SuppressLint("NotifyDataSetChanged")
    fun setPhotoIdList(photoId: List<String>) {
        initData(photoId)
        notifyDataSetChanged()
    }

    override fun bind(binding: ItemSliderPhotoBinding, item: String, position: Int) {
        super.bind(binding, item, position)
        binding.run {
            photoReference = item
            imgPicture.transitionName = item
            root.setOnClickListener { onItemClick.invoke(imgPicture, data, item, position) }
        }
    }
}