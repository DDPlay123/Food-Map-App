package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.ItemFavoriteBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.show
import com.side.project.foodmap.util.Method
import java.util.*

class FavoriteListAdapter : RecyclerView.Adapter<FavoriteListAdapter.ViewHolder>() {

    private val itemCallback = object : DiffUtil.ItemCallback<FavoriteList>() {
        override fun areItemsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        override fun areContentsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((String) -> Unit)
    lateinit var onItemPullFavorite: ((FavoriteList, Int) -> Unit)
    lateinit var onItemWebsite: ((String) -> Unit)
    lateinit var onItemNavigation: ((Double, Double) -> Unit)
    lateinit var onItemPhone: ((String) -> Unit)
    lateinit var onItemShare: ((String) -> Unit)

    fun setData(favoriteList: List<FavoriteList>) = differ.submitList(favoriteList)

    fun getData(position: Int): FavoriteList = differ.currentList[position]


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            binding.placeDetail = getData(position)
            binding.today = Method.getWeekOfDate(Date())

            binding.imgSetFavorite.setOnClickListener { onItemPullFavorite.invoke(getData(position), position) }

            if (getData(position).photos.isNotEmpty()) {
                val favoritePhotosListAdapter = FavoritePhotosListAdapter()
                binding.rvPhotos.apply {
                    layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = favoritePhotosListAdapter
                    favoritePhotosListAdapter.setPhotosList(getData(position).photos)
                    show()
                }
            } else
                binding.rvPhotos.gone()
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)
}