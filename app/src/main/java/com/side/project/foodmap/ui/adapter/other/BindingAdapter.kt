package com.side.project.foodmap.ui.adapter.other

import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import coil.imageLoader
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.appInfo
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.util.tools.Method
import java.text.SimpleDateFormat
import java.util.*

class BindingAdapter {
    companion object {
        @BindingAdapter("android:loadAnyImage")
        @kotlin.jvm.JvmStatic
        fun setLoadImage(imageView: ImageView, any: Any) {
            try {
                imageView.load(any, imageLoader = imageView.context.imageLoader) {
                    scale(Scale.FILL)
                }
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
                    scale(Scale.FILL)
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
                    scale(Scale.FILL)
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
                    scale(Scale.FILL)
                }
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:loadSquareImageFromGoogle")
        @kotlin.jvm.JvmStatic
        fun setLoadSquareImageFromGoogle(imageView: ImageView, photoReference: String) {
            try {
                val maxWidth = 400
                imageView.load(
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=" +
                            "$photoReference&key=${imageView.context.appInfo().metaData["GOOGLE_KEY"].toString()}",
                    imageLoader = imageView.context.imageLoader
                ) {
                    scale(Scale.FILL)
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

        @BindingAdapter("android:edImgTool")
        @kotlin.jvm.JvmStatic
        fun edImgTool(imageView: ImageView, editText: EditText) {
            try {
                // init
                imageView.setOnClickListener { editText.setText("") }
                // track
                editText.addTextChangedListener {
                    if (it?.isNotEmpty() == true)
                        imageView.setImageResource(R.drawable.ic_cancel)
                    else
                        imageView.setImageResource(R.drawable.ic_microphone)
                }
            } catch (e: Exception) {
            }
        }

        @BindingAdapter("android:edImgClear")
        @kotlin.jvm.JvmStatic
        fun edImgClear(imageView: ImageView, editText: EditText) {
            try {
                // init
                imageView.setOnClickListener { editText.setText("") }
                // track
                editText.addTextChangedListener {
                    if (it?.isNotEmpty() == true)
                        imageView.display()
                    else
                        imageView.hidden()
                }
            } catch (e: Exception) {
            }
        }

        @BindingAdapter("android:showDistance")
        @kotlin.jvm.JvmStatic
        fun showDistance(textView: TextView, distance: Double) {
            try {
                textView.text = String.format(
                    textView.context.getString(if (distance < 1) R.string.text_number_meter else R.string.text_number_kilometer),
                    if (distance < 1) distance * 1000 else distance)
            } catch (e: Exception) {
            }
        }
    }
}