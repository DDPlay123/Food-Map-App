package com.side.project.foodmap.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import coil.imageLoader
import coil.load
import coil.transform.RoundedCornersTransformation
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter
import com.alexvasilkov.gestures.views.GestureImageView
import com.side.project.foodmap.data.remote.google.placesDetails.Photo
import com.side.project.foodmap.helper.appInfo
import com.side.project.foodmap.util.gestureViewsSetting.SettingsController

class SlidePhotoAdapter(private val pager: ViewPager, private val controller: SettingsController) : RecyclePagerAdapter<SlidePhotoAdapter.ViewHolder>() {
    private var photos = ArrayList<Photo>()

    @SuppressLint("NotifyDataSetChanged")
    fun setPhotoList(photoList: List<Photo>) {
        this.photos = photoList as ArrayList<Photo>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(container: ViewGroup): ViewHolder {
        val holder = ViewHolder(container)
        holder.image.controller.enableScrollInViewPager(pager)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        controller.apply(holder.image)
        val maxWidth = 400
        holder.image.load(
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=" +
                    "${photos[position].photo_reference}&key=${holder.image.context.appInfo().metaData["GOOGLE_KEY"].toString()}",
            imageLoader = holder.image.context.imageLoader
        )
    }

    override fun getCount(): Int = photos.size

    class ViewHolder(container: ViewGroup) :
        RecyclePagerAdapter.ViewHolder(GestureImageView(container.context)) {
        val image: GestureImageView = itemView as GestureImageView
    }
}