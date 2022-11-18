package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.databinding.ItemPromptSelectBinding
import com.side.project.foodmap.helper.gone

class WorkDayAdapter : RecyclerView.Adapter<WorkDayAdapter.ViewHolder>() {
    private var workday = ArrayList<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun setWorkdayList(regionList: List<String>) {
        this.workday = regionList as ArrayList<String>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemPromptSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.run {
            imgCheck.gone()
            itemName = workday[position]
            isFinal = position == workday.size - 1
        }
    }

    override fun getItemCount(): Int  = workday.size

    class ViewHolder(val binding: ItemPromptSelectBinding): RecyclerView.ViewHolder(binding.root)
}