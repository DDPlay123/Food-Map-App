package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.data.remote.api.Location
import com.side.project.foodmap.databinding.ItemFavoriteBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.util.tools.Method
import java.util.*

class FavoriteListAdapter : RecyclerView.Adapter<FavoriteListAdapter.ViewHolder>() {

    private val itemCallback = object : DiffUtil.ItemCallback<FavoriteList>() {
        override fun areItemsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem.place_id == newItem.place_id
        }

        override fun areContentsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((FavoriteList) -> Unit)
    lateinit var onItemPullFavorite: ((FavoriteList) -> Unit)
    lateinit var onItemWebsite: ((String) -> Unit)
    lateinit var onItemDetail: ((String) -> Unit)
    lateinit var onItemPhone: ((String) -> Unit)
    lateinit var onItemShare: ((String) -> Unit)

    fun setData(favoriteList: List<FavoriteList>) = differ.submitList(favoriteList)

    fun getData(position: Int): FavoriteList = differ.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            binding.executePendingBindings()
            binding.placeDetail = getData(adapterPosition)
            binding.today = Method.getWeekOfDate(Date()) - 1

            binding.root.setOnClickListener { onItemClick.invoke(getData(adapterPosition)) }
            binding.imgSetFavorite.setOnClickListener { onItemPullFavorite.invoke(getData(adapterPosition)) }
            binding.btnWebsite.setOnClickListener { onItemWebsite.invoke(getData(adapterPosition).website) }
            binding.btnDetail.setOnClickListener { onItemDetail.invoke(getData(adapterPosition).place_id) }
            binding.btnPhone.setOnClickListener { onItemPhone.invoke(getData(adapterPosition).phone) }
            binding.btnShare.setOnClickListener { onItemShare.invoke(getData(adapterPosition).url) }

            if (getData(adapterPosition).photos != null) {
                val favoritePhotosListAdapter = FavoritePhotosListAdapter()
                binding.rvPhotos.apply {
                    layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = favoritePhotosListAdapter
                    getData(adapterPosition).photos?.let { favoritePhotosListAdapter.setPhotosList(it) }
                    display()
                }
            } else
                binding.rvPhotos.gone()
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)
}