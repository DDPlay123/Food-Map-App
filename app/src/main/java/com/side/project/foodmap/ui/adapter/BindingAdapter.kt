package com.side.project.foodmap.ui.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.imageLoader
import coil.load
import com.side.project.foodmap.helper.appInfo

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

        @BindingAdapter("android:loadImageFromGoogle")
        @kotlin.jvm.JvmStatic
        fun setLoadImageFromGoogle(imageView: ImageView, photoReference: String) {
            try {
                val maxWidth = 400
                imageView.load(
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=" +
                            "$photoReference&key=${imageView.context.appInfo().metaData["GOOGLE_KEY"].toString()}",
                    imageLoader = imageView.context.imageLoader
                )
            } catch (ignored: Exception) {
            }
        }
    }
}