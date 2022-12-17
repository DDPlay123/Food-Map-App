package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.google.placesDetails.Photo
import com.side.project.foodmap.databinding.ItemSliderPhotoBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class DetailPhotoAdapter : BaseRvAdapter<ItemSliderPhotoBinding, String>(R.layout.item_slider_photo) {

    lateinit var onItemClick: ((String, Int) -> Unit)

    @SuppressLint("NotifyDataSetChanged")
    fun setPhotoIdList(photoId: List<String>) {
        initData(photoId)
        notifyDataSetChanged()
    }

    override fun bind(binding: ItemSliderPhotoBinding, item: String, position: Int) {
        super.bind(binding, item, position)
        binding.run {
            binding.photoReference = item
            binding.root.setOnClickListener { onItemClick.invoke(item, position) }
        }
    }
}