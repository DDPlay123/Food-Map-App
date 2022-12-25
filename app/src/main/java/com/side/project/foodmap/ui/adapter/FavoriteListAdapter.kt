package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.ItemFavoriteBinding
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.getDrawableCompat
import com.side.project.foodmap.util.tools.Method
import java.util.*

class FavoriteListAdapter : RecyclerView.Adapter<FavoriteListAdapter.ViewHolder>(), Filterable {

    private val itemCallback = object : DiffUtil.ItemCallback<FavoriteList>() {
        override fun areItemsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem.place_id == newItem.place_id
        }

        override fun areContentsTheSame(oldItem: FavoriteList, newItem: FavoriteList): Boolean {
            return oldItem == newItem
        }
    }

    private var favoriteList = listOf<FavoriteList>()
    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onPhotoItemClick: ((View, List<String>, String, Int) -> Unit)
    lateinit var onItemClick: ((FavoriteList) -> Unit)
    lateinit var onItemPullFavorite: ((FavoriteList) -> Unit)
    lateinit var onItemWebsite: ((String) -> Unit)
    lateinit var onItemDetail: ((String) -> Unit)
    lateinit var onItemPhone: ((String) -> Unit)
    lateinit var onItemShare: ((String) -> Unit)

    fun setData(favoriteList: List<FavoriteList>) {
        this.favoriteList = favoriteList
        differ.submitList(favoriteList)
    }

    fun getData(position: Int): FavoriteList = differ.currentList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            binding.executePendingBindings()
            binding.placeDetail = getData(absoluteAdapterPosition)
            binding.today = Method.getWeekOfDate(Date()) - 1

            binding.apply {
                if (getData(absoluteAdapterPosition).website.isEmpty())
                    btnWebsite.background = btnWebsite.context.getDrawableCompat(R.drawable.background_google_gray_button)
                if (getData(absoluteAdapterPosition).phone.isEmpty())
                    btnPhone.background = btnPhone.context.getDrawableCompat(R.drawable.background_google_gray_button)
            }

            binding.root.setOnClickListener { onItemClick.invoke(getData(absoluteAdapterPosition)) }
            binding.imgSetFavorite.setOnClickListener { onItemPullFavorite.invoke(getData(absoluteAdapterPosition)) }
            binding.btnWebsite.setOnClickListener { onItemWebsite.invoke(getData(absoluteAdapterPosition).website) }
            binding.btnDetail.setOnClickListener { onItemDetail.invoke(getData(absoluteAdapterPosition).place_id) }
            binding.btnPhone.setOnClickListener { onItemPhone.invoke(getData(absoluteAdapterPosition).phone) }
            binding.btnShare.setOnClickListener { onItemShare.invoke(getData(absoluteAdapterPosition).url) }

            if (!getData(absoluteAdapterPosition).photos.isNullOrEmpty()) {
                val favoritePhotosListAdapter = FavoritePhotosListAdapter()
                binding.rvPhotos.apply {
                    layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = favoritePhotosListAdapter
                    getData(absoluteAdapterPosition).photos?.let { favoritePhotosListAdapter.setPhotosList(it) }
                    display()

                    favoritePhotosListAdapter.onItemClick = { imgView, photos, photo, position ->
                        onPhotoItemClick.invoke(imgView, photos, photo, position)
                    }
                }
            } else
                binding.rvPhotos.gone()
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getFilter(): Filter = customFilter

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList = mutableListOf<FavoriteList>()
            val filterPattern = constraint.toString().lowercase().trim()
            if (constraint.isEmpty() || filterPattern == "")
                filteredList.addAll(favoriteList)
            else
                favoriteList.forEach { item ->
                    if (item.name.lowercase().trim().contains(filterPattern))
                        filteredList.add(item)
                }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, filterResults: FilterResults) {
            differ.submitList(filterResults.values as MutableList<FavoriteList>)
        }
    }

    class ViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)
}