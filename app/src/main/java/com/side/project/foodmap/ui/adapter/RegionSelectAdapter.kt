package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.databinding.ItemPromptSelectBinding

class RegionSelectAdapter : RecyclerView.Adapter<RegionSelectAdapter.ViewHolder>() {
    private var regionList = ArrayList<String>()
    private var selectPosition = -1

    lateinit var onItemClick: ((String) -> Unit)

    @SuppressLint("NotifyDataSetChanged")
    fun setRegionList(regionList: ArrayList<String>, position: Int) {
        this.regionList = regionList
        selectPosition = position
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemPromptSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.run {
            // initialize
            isFinal = position == regionList.size - 1
            itemName = regionList[position]
            isCheck = selectPosition == position
            // listener
            root.setOnClickListener {
                onItemClick.invoke(regionList[position])
            }
        }
    }

    override fun getItemCount(): Int  = regionList.size

    class ViewHolder(val binding: ItemPromptSelectBinding): RecyclerView.ViewHolder(binding.root)
}