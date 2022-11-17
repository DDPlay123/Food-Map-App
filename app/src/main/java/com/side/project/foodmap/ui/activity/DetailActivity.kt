package com.side.project.foodmap.ui.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.google.placesDetails.Photo
import com.side.project.foodmap.data.remote.google.placesDetails.Result
import com.side.project.foodmap.databinding.ActivityDetailBinding
import com.side.project.foodmap.helper.appInfo
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.getDrawableCompat
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.ui.adapter.DetailPhotoAdapter
import com.side.project.foodmap.ui.adapter.PopularSearchAdapter
import com.side.project.foodmap.ui.viewModel.DetailViewModel
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModel()

    private lateinit var placeId: String
    private lateinit var detailPhotoAdapter: DetailPhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding.paddingTop = getStatusBarHeight()

        intent.extras?.let {
            placeId = it.getString(PLACE_ID, "") ?: ""
            viewModel.searchDetail(placeId, appInfo().metaData["GOOGLE_KEY"].toString())
        }

        doInitialize()
        setListener()
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0)
            result = resources.getDimensionPixelSize(resourceId)

        return result
    }

    private fun doInitialize() {
        lifecycleScope.launchWhenCreated {
            viewModel.searchDetailState.collect {
                when (it) {
                    is Resource.Loading -> {
                        Method.logE("Search Detail", "Loading")
                        dialog.showLoadingDialog(false)
                    }
                    is Resource.Success -> {
                        Method.logE("Search Detail", "Success")
                        dialog.cancelLoadingDialog()
                        it.data?.result?.let { data ->
                            setupData(data)
                        }
                    }
                    is Resource.Error -> {
                        Method.logE("Search Detail", "Error:${it.message.toString()}")
                        dialog.cancelLoadingDialog()
                        displayShortToast(getString(R.string.hint_error))
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupData(data: Result) {
        binding.run {
            detail = data
            data.photos?.let { photoList -> initPhotoSlider(photoList) }
        }
    }

    private fun setListener() {
        binding.run {

        }
    }

    private fun initPhotoSlider(photos: List<Photo>) {
        detailPhotoAdapter = DetailPhotoAdapter()
        binding.vpPhoto.apply {
            offscreenPageLimit = 1
            adapter = detailPhotoAdapter
            detailPhotoAdapter.setData(photos)
            setupSliderIndicators(photos.size)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentSliderIndicator(position)
                }
            })
        }
    }

    private fun setupSliderIndicators(count: Int) {
        binding.run {
            val indicators: Array<AppCompatImageView?> = arrayOfNulls(count)
            val layoutParams: LinearLayoutCompat.LayoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(10, 0, 10, 0)

            indicators.forEachIndexed { id, _ ->
                indicators[id] = AppCompatImageView(applicationContext)
                indicators[id]?.setImageDrawable(applicationContext.getDrawableCompat(R.drawable.background_slider_indicator_inactive))

                indicators[id]?.layoutParams = layoutParams
                sliderIndicators.addView(indicators[id])
            }

            setCurrentSliderIndicator(0)
        }
    }

    private fun setCurrentSliderIndicator(position: Int) {
        binding.run {
            val childCount: Int = sliderIndicators.childCount
            for (i in 0 until childCount) {
                val imageView: AppCompatImageView = sliderIndicators.getChildAt(i) as AppCompatImageView
                if (i == position)
                    imageView.setImageDrawable(applicationContext.getDrawableCompat(R.drawable.background_slider_indicator_active))
                else
                    imageView.setImageDrawable(applicationContext.getDrawableCompat(R.drawable.background_slider_indicator_inactive))
            }
        }
    }

    companion object {
        private const val PLACE_ID = "PLACE_ID"
    }
}