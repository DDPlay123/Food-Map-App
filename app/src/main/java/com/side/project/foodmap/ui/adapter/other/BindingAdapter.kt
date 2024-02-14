package com.side.project.foodmap.ui.adapter.other

import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import coil.imageLoader
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.util.Constants.BASE_URL
import com.side.project.foodmap.util.tools.Method
import java.text.SimpleDateFormat
import java.util.*

class BindingAdapter {
    companion object {
        @BindingAdapter("android:loadAnyImage")
        @JvmStatic
        fun setLoadImage(imageView: ImageView, any: Any) {
            try {
                imageView.load(any, imageLoader = imageView.context.imageLoader)
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:loadUserCircleAnyImage")
        @JvmStatic
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
        @JvmStatic
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
        @JvmStatic
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

        @BindingAdapter("android:loadSquareImageFromGoogle")
        @JvmStatic
        fun setLoadSquareImageFromGoogle(imageView: ImageView, photoReference: String) {
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

        @BindingAdapter("android:loadImageFromApi")
        @JvmStatic
        fun setLoadImageFromApi(imageView: ImageView, photoId: String) {
            try {
                val userID = ""
                imageView.load("${BASE_URL}api/place/get_html_photo/$photoId?userId=$userID", imageLoader = imageView.context.imageLoader
                ) {
                    transformations(RoundedCornersTransformation(25f))
                }
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:loadSquareImageFromApi")
        @JvmStatic
        fun setLoadSquareImageFromApi(imageView: ImageView, photoId: String) {
            try {
                val userID = ""
                imageView.load("${BASE_URL}api/place/get_html_photo/$photoId?userId=$userID", imageLoader = imageView.context.imageLoader)
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:getDateFormat")
        @JvmStatic
        fun getDataFormat(textView: TextView, unixTime: String) {
            try {
                val simpleDate = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.TAIWAN)
                textView.text = simpleDate.format(Date(unixTime.toLong() * 1000L))
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:edImgSoundTool")
        @JvmStatic
        fun edImgSoundTool(imageView: ImageView, editText: EditText) {
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
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:edImgHideTool")
        @JvmStatic
        fun edImgHideTool(imageView: ImageView, editText: EditText) {
            try {
                // track
                editText.addTextChangedListener {
                    if (it?.isNotEmpty() == true)
                        imageView.hidden()
                    else
                        imageView.display()
                }
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:edImgClear")
        @JvmStatic
        fun edImgClear(imageView: ImageView, editText: EditText) {
            try {
                // init
                imageView.setOnClickListener { editText.setText("") }
                // track
                editText.addTextChangedListener {
                    if (it?.isNotEmpty() == true)
                        imageView.display()
                    else
                        imageView.gone()
                }
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:showDistance")
        @JvmStatic
        fun showDistance(textView: TextView, distance: Double) {
            try {
                textView.text = String.format(
                    textView.context.getString(if (distance < 1) R.string.text_number_meter else R.string.text_number_kilometer),
                    if (distance < 1) distance * 1000 else distance)
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:showDistanceByApi")
        @JvmStatic
        fun showDistanceByApi(textView: TextView, distance: Double) {
            try {
                textView.text = String.format(
                    textView.context.getString(if (distance < 1000) R.string.text_number_meter else R.string.text_number_kilometer),
                    if (distance < 1000) distance else distance / 1000)
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:imgTrackLocation")
        @JvmStatic
        fun imgTrackLocation(imageView: ImageView, isTrack: Boolean) {
            try {
                if (isTrack) imageView.setImageResource(R.drawable.ic_my_location)
                else imageView.setImageResource(R.drawable.ic_location_searching)
            } catch (ignored: Exception) {
            }
        }

        @BindingAdapter("android:tvTrackLocation")
        @JvmStatic
        fun tvTrackLocation(textView: TextView, isTrack: Boolean) {
            try {
                textView.apply {
                    if (isTrack) {
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, 0, 0)
                        text = textView.context.getText(R.string.hint_is_track_location)
                        setTextColor(context.getColorCompat(R.color.red))
                    } else {
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open, 0, 0, 0)
                        text = textView.context.getText(R.string.hint_no_track_location)
                        setTextColor(context.getColorCompat(R.color.green))
                    }
                }
            } catch (ignored: Exception) {
            }
        }
    }
}