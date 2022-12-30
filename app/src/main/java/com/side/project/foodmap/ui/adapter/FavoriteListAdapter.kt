package com.side.project.foodmap.ui.adapter

import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.ItemFavoriteBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.getDrawableCompat
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter
import com.side.project.foodmap.util.tools.Method
import java.util.*

class FavoriteListAdapter :
    BaseRvListAdapter<ItemFavoriteBinding, FavoriteList>(R.layout.item_favorite, ItemCallback()), Filterable {

    class ItemCallback : DiffUtil.ItemCallback<FavoriteList>() {
        override fun areItemsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem.place_id == newItem.place_id
        }

        override fun areContentsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem == newItem
        }
    }

    lateinit var onPhotoItemClick: ((List<String>, Int) -> Unit)
    lateinit var onItemClick: ((FavoriteList) -> Unit)
    lateinit var onItemPullFavorite: ((FavoriteList) -> Unit)
    lateinit var onItemWebsite: ((String) -> Unit)
    lateinit var onItemDetail: ((String) -> Unit)
    lateinit var onItemPhone: ((String) -> Unit)
    lateinit var onItemShare: ((String) -> Unit)

    override fun bind(item: FavoriteList, binding: ItemFavoriteBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            executePendingBindings()
            placeDetail = item
            today = Method.getWeekOfDate(Date()) - 1

            apply {
                if (item.website.isEmpty())
                    btnWebsite.background = btnWebsite.context.getDrawableCompat(R.drawable.background_google_gray_button)
                if (item.phone.isEmpty())
                    btnPhone.background = btnPhone.context.getDrawableCompat(R.drawable.background_google_gray_button)
            }

            root.setOnClickListener { onItemClick.invoke(item) }
            imgSetFavorite.setOnClickListener { onItemPullFavorite.invoke(item) }
            btnWebsite.setOnClickListener { onItemWebsite.invoke(item.website) }
            btnDetail.setOnClickListener { onItemDetail.invoke(item.place_id) }
            btnPhone.setOnClickListener { onItemPhone.invoke(item.phone) }
            btnShare.setOnClickListener { onItemShare.invoke(item.url) }

            if (!item.photos.isNullOrEmpty()) {
                val favoritePhotosListAdapter = FavoritePhotosListAdapter()
                rvPhotos.apply {
                    layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = favoritePhotosListAdapter
                    item.photos.let { favoritePhotosListAdapter.submitList(it.toMutableList()) }
                    display()

                    favoritePhotosListAdapter.onItemClick = { photos, position ->
                        onPhotoItemClick.invoke(photos, position)
                    }
                }
            } else
                rvPhotos.gone()
        }
    }

    override fun getFilter(): Filter = customFilter

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList = mutableListOf<FavoriteList>()
            val filterPattern = constraint.toString().lowercase().trim()
            if (constraint.isEmpty() || filterPattern == "")
                filteredList.addAll(currentList)
            else
                currentList.forEach { item ->
                    if (item.name.lowercase().trim().contains(filterPattern))
                        filteredList.add(item)
                }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, filterResults: FilterResults) {
            submitList(filterResults.values as MutableList<FavoriteList>)
        }
    }
}