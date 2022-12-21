package com.side.project.foodmap.ui.fragment.other

import android.os.Bundle
import android.view.Gravity
import com.alexvasilkov.gestures.Settings
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentImageBinding
import com.side.project.foodmap.util.Constants.IMAGE_RESOURCE

class ImageFragment : BaseFragment<FragmentImageBinding>(R.layout.fragment_image) {

    companion object {
        @JvmStatic
        fun newInstance(photoId: String) = ImageFragment().apply {
            arguments = Bundle().apply {
                putString(IMAGE_RESOURCE, photoId)
            }
        }
    }

    override fun FragmentImageBinding.initialize() {
        val photoId: String = arguments?.getString(IMAGE_RESOURCE, "") ?: ""
        initGestureView()
        binding.imgPicture.transitionName = photoId
        binding.photoReference = photoId
    }

    private fun initGestureView() {
        binding.imgPicture.controller.settings
            .setMaxZoom(6f)
            .setDoubleTapZoom(-1f) // Falls back to max zoom level
            .setPanEnabled(true)
            .setZoomEnabled(true)
            .setDoubleTapEnabled(true)
            .setRotationEnabled(false)
            .setRestrictRotation(false)
            .setOverscrollDistance(0f, 0f)
            .setOverzoomFactor(2f)
            .setFillViewport(false)
            .setFitMethod(Settings.Fit.INSIDE)
            .gravity = Gravity.CENTER
    }
}