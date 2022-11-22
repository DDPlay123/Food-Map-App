package com.side.project.foodmap.ui.adapter.other

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.imageLoader
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.appInfo
import com.side.project.foodmap.util.Method
import java.text.SimpleDateFormat
import java.util.*

class BindingAdapter {
    companion object {
        @BindingAdapter("android:loadAnyImage")
        @kotlin.jvm.JvmStatic
        fun setLoadImage(imageView: ImageView, any: Any) {
            try {
                imageView.load(any, imageLoader = imageView.context.imageLoader)
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:loadUserCircleAnyImage")
        @kotlin.jvm.JvmStatic
        fun setLoadUserCircleImage(imageView: ImageView, any: Any) {
            try {
                imageView.load(any, imageLoader = imageView.context.imageLoader) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.img_user)
                    error(R.drawable.img_user)
                }
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:loadUserPicture")
        @kotlin.jvm.JvmStatic
        fun setLoadUserPicture(imageView: ImageView, picture: String) {
            try {
                val image = Method.decodeImage(picture)
                imageView.load(image) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.img_user)
                    error(R.drawable.img_user)
                }
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
                ) {
                    transformations(RoundedCornersTransformation(25f))
                }
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:getDateFormat")
        @kotlin.jvm.JvmStatic
        fun getDataFormat(textView: TextView, unixTime: String) {
            try {
                val simpleDate = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.TAIWAN)
                textView.text = simpleDate.format(Date(unixTime.toLong() * 1000L))
            } catch (ignored: Exception) {
            }
        }
    }
}