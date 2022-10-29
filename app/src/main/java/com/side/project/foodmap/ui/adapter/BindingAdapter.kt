package com.side.project.foodmap.ui.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.imageLoader
import coil.load

class BindingAdapter {
    companion object {
        @BindingAdapter("android:loadImage")
        @kotlin.jvm.JvmStatic
        fun setLoadImage(imageView: ImageView, any: Any) {
            try {
                imageView.load(any, imageLoader = imageView.context.imageLoader)
            } catch (ignored: Exception) {
            }
        }
    }
}