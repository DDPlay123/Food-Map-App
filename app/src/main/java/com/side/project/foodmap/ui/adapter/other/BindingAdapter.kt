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
import com.side.project.foodmap.data.remote.api.restaurant.GetPhotoReq
import com.side.project.foodmap.data.remote.api.restaurant.GetPhotoRes
import com.side.project.foodmap.helper.appInfo
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.tools.Method
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

        @BindingAdapter("android:loadImageFromApi")
        @kotlin.jvm.JvmStatic
        fun setLoadImageFromApi(imageView: ImageView, photoId: String) {
            try {
                val getPhotoReq = GetPhotoReq(
                    userId = "639c4533a4701cd07fa27123",
                    accessKey = "670e6d62a5315578f8b0f7d5a3e867d",
                    photoId = photoId
                )
                ApiClient.getAPI.apiGetPhoto(getPhotoReq).enqueue(object : Callback<GetPhotoRes> {
                    override fun onResponse(
                        call: Call<GetPhotoRes>,
                        response: Response<GetPhotoRes>
                    ) {
                        response.body()?.let {
                            imageView.load(Method.decodeImage(it.result.data), imageLoader = imageView.context.imageLoader
                            ) {
                                transformations(RoundedCornersTransformation(25f))
                                scale(Scale.FILL)
                            }
                        }
                    }

                    override fun onFailure(call: Call<GetPhotoRes>, t: Throwable) {
                        imageView.load("", imageLoader = imageView.context.imageLoader
                        ) {
                            transformations(RoundedCornersTransformation(25f))
                            scale(Scale.FILL)
                        }
                    }
                })
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

        @BindingAdapter("android:loadSquareImageFromApi")
        @kotlin.jvm.JvmStatic
        fun setLoadSquareImageFromApi(imageView: ImageView, photoId: String) {
            try {
                imageView.load("http://kkhomeserver.ddns.net:33000/api/place/get_html_photo/$photoId",
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

        @BindingAdapter("android:edImgSoundTool")
        @kotlin.jvm.JvmStatic
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
            } catch (e: Exception) {
            }
        }

        @BindingAdapter("android:edImgCameraTool")
        @kotlin.jvm.JvmStatic
        fun edImgCameraTool(imageView: ImageView, editText: EditText) {
            try {
                // track
                editText.addTextChangedListener {
                    if (it?.isNotEmpty() == true)
                        imageView.hidden()
                    else
                        imageView.display()
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