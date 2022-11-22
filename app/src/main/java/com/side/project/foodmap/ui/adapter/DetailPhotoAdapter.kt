package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.google.placesDetails.Photo
import com.side.project.foodmap.databinding.ItemSliderPhotoBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter

class DetailPhotoAdapter : BaseRvAdapter<ItemSliderPhotoBinding, Photo>(R.layout.item_slider_photo) {

    private val itemCallback = object : DiffUtil.ItemCallback<Photo>() {
        // 比對新舊 Item
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.photo_reference == newItem.photo_reference
        }
        // 比對新舊 Item 內容
        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((Photo, Int) -> Unit)

    fun setterData(photoList: List<Photo>) {
        differ.submitList(photoList)
        initData(differ.currentList)
    }

    fun getterData(position: Int): Photo = differ.currentList[position]

    override fun bind(binding: ItemSliderPhotoBinding, item: Photo, position: Int) {
        super.bind(binding, item, position)
        binding.photoReference = item.photo_reference
        binding.root.setOnClickListener { onItemClick.invoke(item, position) }
    }
}