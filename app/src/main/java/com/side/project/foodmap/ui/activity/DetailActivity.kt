package com.side.project.foodmap.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.google.placesDetails.Photo
import com.side.project.foodmap.data.remote.google.placesDetails.Result
import com.side.project.foodmap.data.remote.google.placesDetails.Review
import com.side.project.foodmap.databinding.ActivityDetailBinding
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.adapter.DetailPhotoAdapter
import com.side.project.foodmap.ui.adapter.GoogleReviewsAdapter
import com.side.project.foodmap.ui.adapter.WorkDayAdapter
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.DetailViewModel
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModel()
    private val animManager: AnimManager by inject()

    // Data
    private lateinit var placeId: String
    private lateinit var googleUrl: String
    private lateinit var website: String
    private lateinit var phone: String
    private var workday: List<String> = emptyList()
    // Tool
    private lateinit var detailPhotoAdapter: DetailPhotoAdapter
    private lateinit var googleReviewsAdapter: GoogleReviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding.paddingTop = getStatusBarHeight()

        intent.extras?.let {
            placeId = it.getString(PLACE_ID, "") ?: ""
            viewModel.searchDetail(placeId, appInfo().metaData["GOOGLE_KEY"].toString())
        }

        checkNetWork { onBackPressed() }

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
            data.reviews?.let { reviewsList -> initRvReviews(reviewsList) }
            data.photos?.let { photoList -> initPhotoSlider(photoList) }
            data.opening_hours?.weekday_text?.let { it -> workday = it }
            data.url?.let { it -> googleUrl = it }
            data.website?.let { it -> website = it }
            data.formatted_phone_number?.let { it -> phone = it }
        }
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding.run {
            tvBack.setOnClickListener { it.setAnimClick(anim, AnimState.Start) { onBackPressed() } }

            tvTime.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (workday.isEmpty()) {
                        displayShortToast(getString(R.string.text_null))
                        return@setAnimClick
                    }
                    displayRegionDialog()
                }
            }

            tvGoogle.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::googleUrl.isInitialized) {
                        Intent(Intent.ACTION_VIEW).also { i ->
                            i.data = Uri.parse(googleUrl)
                            startActivity(i)
                        }
                    } else
                        displayShortToast(getString(R.string.hint_no_website))
                }
            }

            btnWebsite.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::website.isInitialized) {
                        Intent(Intent.ACTION_VIEW).also { i ->
                            i.data = Uri.parse(website)
                            startActivity(i)
                        }
                    } else
                        displayShortToast(getString(R.string.hint_no_website))
                }
            }

            btnNavigation.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    // TODO(導航)

                }
            }

            btnFavorite.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    // TODO(添加最愛)
                }
            }

            btnPhone.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::phone.isInitialized) {
                        Intent(Intent.ACTION_DIAL).also { i ->
                            i.data = Uri.parse("tel:$phone")
                            startActivity(i)
                        }
                    } else
                        displayShortToast(getString(R.string.hint_no_phone))
                }
            }
        }
    }

    private fun displayRegionDialog() {
        val dialogBinding = DialogPromptSelectBinding.inflate(layoutInflater)
        val workDayAdapter = WorkDayAdapter()
        dialog.showCenterDialog(true, dialogBinding, false).let {
            dialogBinding.run {
                // initialize
                titleText = getString(R.string.text_workday)
                hideCancel = true
                hideConfirm = true
                listItem.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = workDayAdapter
                    workDayAdapter.setWorkdayList(workday)
                }
            }
        }
    }

    private fun initRvReviews(review: List<Review>) {
        googleReviewsAdapter = GoogleReviewsAdapter()
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = googleReviewsAdapter
            googleReviewsAdapter.setData(review)
        }

        googleReviewsAdapter.onItemClick = {
            Intent(Intent.ACTION_VIEW).also { i ->
                i.data = Uri.parse(it.author_url)
                startActivity(i)
            }
        }
    }

    private fun initPhotoSlider(photos: List<Photo>) {
        detailPhotoAdapter = DetailPhotoAdapter()
        binding.vpPhoto.apply {
            offscreenPageLimit = 1
            adapter = detailPhotoAdapter
            detailPhotoAdapter.setData(photos)
            setupSliderIndicators(photos.size)

            if (photos.isEmpty()) {
                binding.imgPlaceHolder.show()
                binding.vpPhoto.hidden()
            } else {
                binding.imgPlaceHolder.hidden()
                binding.vpPhoto.show()
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentSliderIndicator(position)
                }
            })
        }

        detailPhotoAdapter.onItemClick = { photo, position ->
            // TODO(切換大圖)
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