package com.side.project.foodmap.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.PlaceList
import com.side.project.foodmap.databinding.ItemRestaurantViewBinding
import com.side.project.foodmap.ui.adapter.other.BaseRvAdapter
import java.io.IOException

class RestaurantListAdapter : BaseRvAdapter<ItemRestaurantViewBinding, PlaceList>(R.layout.item_restaurant_view) {

    private val itemCallback = object : DiffUtil.ItemCallback<PlaceList>() {
        // 比對新舊 Item
        override fun areItemsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem.uid == newItem.uid
        }

        // 比對新舊 Item 內容
        override fun areContentsTheSame(oldItem: PlaceList, newItem: PlaceList): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, itemCallback)

    lateinit var onItemClick: ((String) -> Unit)

    fun setterData(placeList: ArrayList<PlaceList>) {
        differ.submitList(placeList)
        initData(differ.currentList)
    }

    fun getterData(position: Int): PlaceList = differ.currentList[position]

    override fun ItemRestaurantViewBinding.initialize(binding: ItemRestaurantViewBinding) {
        val params = binding.cardView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.cardView.layoutParams = params
    }

    override fun bind(binding: ItemRestaurantViewBinding, item: PlaceList, position: Int) {
        super.bind(binding, item, position)
        try {
            binding.data = item
            binding.photoReference = if (item.photos != null && item.photos.size > 0)
                item.photos[0].photo_reference
            else
                ""
            binding.root.setOnClickListener { onItemClick.invoke(item.uid) }
        } catch (ignored: IOException) {
        }
    }
}