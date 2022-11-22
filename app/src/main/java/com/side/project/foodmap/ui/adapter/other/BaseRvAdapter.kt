package com.side.project.foodmap.ui.adapter.other

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRvAdapter<T : ViewDataBinding, R : Any>(@LayoutRes val layoutRes: Int)
    : RecyclerView.Adapter<BaseRvAdapter.BaseViewHolder<T>>() {

    var data: List<R> = emptyList()

    fun initData(data: List<R>) {
        this.data = data
    }

    open fun T.initialize() {}

    open fun bind(binding: T, item: R, position: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val binding: T = DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutRes, parent, false)
        binding.initialize()
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) =
        bind(holder.binding, data[position], position)

    override fun getItemCount(): Int = data.size

    class BaseViewHolder<T: ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)
}