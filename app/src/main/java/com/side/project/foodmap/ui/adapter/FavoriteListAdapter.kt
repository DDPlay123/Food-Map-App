package com.side.project.foodmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.ItemFavoriteBinding
import com.side.project.foodmap.helper.getDrawableCompat
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.util.tools.Method
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
    lateinit var onItemPullFavorite: ((FavoriteList) -> Unit)
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
            binding.executePendingBindings()
            binding.placeDetail = getData(adapterPosition)
            binding.today = Method.getWeekOfDate(Date()) - 1

            // Price Level
            val indicators: Array<AppCompatImageView?> = arrayOfNulls(getData(adapterPosition).price_level)
            val layoutParams: LinearLayoutCompat.LayoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 0)
            indicators.forEachIndexed { id, _ ->
                indicators[id] = AppCompatImageView(binding.root.context)
                indicators[id]?.setImageDrawable(binding.root.context.getDrawableCompat(R.drawable.ic_money))

                indicators[id]?.layoutParams = layoutParams
                binding.priceLevelIndicators.addView(indicators[id])
            }

            binding.imgSetFavorite.setOnClickListener { onItemPullFavorite.invoke(getData(adapterPosition)) }

            if (getData(adapterPosition).photos.isNotEmpty()) {
                val favoritePhotosListAdapter = FavoritePhotosListAdapter()
                binding.rvPhotos.apply {
                    layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = favoritePhotosListAdapter
                    favoritePhotosListAdapter.setPhotosList(getData(adapterPosition).photos)
                    display()
                }
            } else
                binding.rvPhotos.gone()
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class ViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)
}