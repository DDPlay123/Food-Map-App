package com.side.project.foodmap.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import coil.imageLoader
import coil.load
import coil.transform.RoundedCornersTransformation
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.MyPlaceList
import com.side.project.foodmap.databinding.ItemRegionBinding
import com.side.project.foodmap.helper.appInfo
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class RegionSelectAdapter :
    BaseRvListAdapter<ItemRegionBinding, MyPlaceList>(R.layout.item_region, ItemCallback()) {

    class ItemCallback : DiffUtil.ItemCallback<MyPlaceList>() {
        override fun areItemsTheSame(oldItem: MyPlaceList, newItem: MyPlaceList): Boolean {
            return oldItem.place_id == newItem.place_id
        }

        override fun areContentsTheSame(oldItem: MyPlaceList, newItem: MyPlaceList): Boolean {
            return oldItem == newItem
        }
    }

    private var selectRegion = ""

    lateinit var onItemClick: ((MyPlaceList, Int) -> Unit)
    lateinit var onDeleteClick: ((MyPlaceList, Int) -> Unit)

    fun setSelectRegion(region: String) {
        selectRegion = region
    }

    override fun bind(item: MyPlaceList, binding: ItemRegionBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            isFirst = selectRegion == item.place_id
            myPlaceList = item
            isCheck = selectRegion == item.place_id
            layoutSwipe.setLockDrag(selectRegion == item.place_id)
            layoutBody.setOnClickListener { onItemClick.invoke(item, position) }
            layoutDelete.setOnClickListener { onDeleteClick.invoke(item, position) }

            imgMap.load(
                data = "http://maps.google.com/maps/api/staticmap?center=${item.location.lat},${item.location.lng}" +
                        "&zoom=15&size=400x100&sensor=false&markers=color:red%7Clabel:O%7C${item.location.lat},${item.location.lng}" +
                        "&key=${imgMap.context.appInfo().metaData["com.google.android.geo.API_KEY"].toString()}",
                imageLoader = imgMap.context.imageLoader
            ) {
                transformations(RoundedCornersTransformation(25f))
            }
        }
    }
}