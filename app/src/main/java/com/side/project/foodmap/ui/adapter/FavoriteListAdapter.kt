package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.ItemFavoriteBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.helper.show
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter
import com.side.project.foodmap.util.Method
import java.util.*
import kotlin.collections.ArrayList

class FavoriteListAdapter : BaseRvAdapter<ItemFavoriteBinding, FavoriteList>(R.layout.item_favorite) {

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

    fun setterData(favoriteList: List<FavoriteList>) {
        differ.submitList(favoriteList)
        initData(differ.currentList)
    }

    fun getterData(position: Int): FavoriteList = differ.currentList[position]

    override fun bind(binding: ItemFavoriteBinding, item: FavoriteList, position: Int) {
        super.bind(binding, item, position)
        binding.placeDetail = item
        binding.today = Method.getWeekOfDate(Date())

        binding.imgSetFavorite.setOnClickListener { onItemPullFavorite.invoke(item, position) }

        if (item.photos.isNotEmpty()) {
            val favoritePhotosListAdapter = FavoritePhotosListAdapter()
            binding.rvPhotos.apply {
                layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = favoritePhotosListAdapter
                favoritePhotosListAdapter.setPhotosList(item.photos)
                show()
            }
        } else
            binding.rvPhotos.gone()
    }
}