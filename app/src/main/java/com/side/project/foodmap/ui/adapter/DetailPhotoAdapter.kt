package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.google.placesDetails.Photo
import com.side.project.foodmap.databinding.ItemSliderPhotoBinding

class DetailPhotoAdapter : RecyclerView.Adapter<DetailPhotoAdapter.ViewHolder>() {

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

    fun setData(photoList: List<Photo>) = differ.submitList(photoList)

    fun getData(position: Int): Photo = differ.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder  =
        ViewHolder(ItemSliderPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.photoReference = getData(position).photo_reference
        holder.binding.root.setOnClickListener { onItemClick.invoke(getData(position), position) }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemSliderPhotoBinding): RecyclerView.ViewHolder(binding.root)
}