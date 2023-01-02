package com.side.project.foodmap.ui.adapter.other

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class BaseViewHolder<VB : ViewDataBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

class BaseItemCallback<R : Any> : DiffUtil.ItemCallback<R>() {
    override fun areItemsTheSame(oldItem: R, newItem: R): Boolean =
        oldItem.toString() == newItem.toString()

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: R, newItem: R): Boolean =
        oldItem == newItem
}

abstract class BaseRvListAdapter<VB : ViewDataBinding, R : Any>(
    @LayoutRes val layoutRes: Int,
    itemCallback : DiffUtil.ItemCallback<R> = BaseItemCallback()
) : ListAdapter<R, BaseViewHolder<VB>>(itemCallback) {

    open fun initialize(binding: VB) {}

    open fun bind(item: R, binding: VB, position: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding: VB = DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutRes, parent, false)
        initialize(binding)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) =
        bind(getItem(holder.absoluteAdapterPosition), holder.binding, holder.absoluteAdapterPosition)

    override fun getItemViewType(position: Int): Int = layoutRes

    override fun getItemCount(): Int = currentList.size
}