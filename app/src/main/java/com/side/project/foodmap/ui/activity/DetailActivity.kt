package com.side.project.foodmap.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.data.remote.google.placesDetails.Photo
import com.side.project.foodmap.data.remote.google.placesDetails.PlacesDetails
import com.side.project.foodmap.data.remote.google.placesDetails.Result
import com.side.project.foodmap.data.remote.google.placesDetails.Review
import com.side.project.foodmap.databinding.ActivityDetailBinding
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.adapter.DetailPhotoAdapter
import com.side.project.foodmap.ui.adapter.GoogleReviewsAdapter
import com.side.project.foodmap.ui.adapter.WorkDayAdapter
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.DetailViewModel
import com.side.project.foodmap.util.Constants.PLACE_ID
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: DetailViewModel
    private val animManager: AnimManager by inject()

    // Data
    private lateinit var placesDetails: Result
    private lateinit var placeId: String
    private lateinit var googleUrl: String
    private lateinit var website: String
    private lateinit var phone: String
    private var photo: MutableList<String> = ArrayList()
    private var workday: List<String> = emptyList()

    // Wait push favorite list
    private lateinit var favoriteList: FavoriteList

    // Tool
    private lateinit var detailPhotoAdapter: DetailPhotoAdapter
    private lateinit var googleReviewsAdapter: GoogleReviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]
        binding.paddingTop = getStatusBarHeight()

        checkNetWork { onBackPressed() }

        getArguments()
        initLocationService()
        doInitialize()
        setListener()
    }

    private fun getArguments() {
        intent.extras?.let {
            placeId = it.getString(PLACE_ID, "") ?: ""
        }
    }

    private fun doInitialize() {
        if (::placeId.isInitialized)
            viewModel.searchDetail(placeId)

        binding.isFavorite = false

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 搜尋詳細資料
                launch {
                    viewModel.searchDetailState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Search Detail", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
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
                // 加入至最愛清單
                launch {
                    viewModel.pushFavoriteState.collect {
                        when (it) {
                            is Resource.Success -> {
                                Method.logE("Push Favorite", "Success")
                                displayShortToast(getString(R.string.text_success))
                                viewModel.insertFavoriteData(favoriteList)
                                binding.isFavorite = true
                            }
                            is Resource.Error -> {
                                Method.logE("Push Favorite", "Error:${it.message.toString()}")
                                displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun setupData(placesDetail: PlacesDetails) {
        val data = placesDetail.result
        binding.run {
            detail = data
            placesDetails = data
            detailFavorite = placesDetail.isFavorite
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
                    if (::googleUrl.isInitialized && googleUrl.isNotEmpty()) {
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
                    if (::website.isInitialized && website.isNotEmpty()) {
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
                    favoriteList = FavoriteList(
                        placeId = placeId,
                        photos = photo,
                        name = placesDetails.name ?: "",
                        latitude = placesDetails.geometry?.location?.lat ?: 0.0,
                        longitude = placesDetails.geometry?.location?.lng ?: 0.0,
                        price_level = placesDetails.price_level ?: 0,
                        url = placesDetails.url ?: "",
                        vicinity = placesDetails.vicinity ?: "",
                        workDay = workday,
                        dine_in = placesDetails.dine_in ?: false,
                        takeout = placesDetails.takeout ?: false,
                        delivery = placesDetails.delivery ?: false,
                        website = placesDetails.website ?: "",
                        phone = placesDetails.formatted_phone_number ?: "",
                        rating = (placesDetails.rating ?: 0.0).toFloat(),
                        ratings_total = (placesDetails.user_ratings_total ?: 0).toLong()
                    )
                    viewModel.pushFavorite(arrayListOf(favoriteList))
                }
            }

            btnPhone.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::phone.isInitialized && phone.isNotEmpty()) {
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
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
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
            googleReviewsAdapter.setReviewList(review)
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
                binding.imgPlaceHolder.display()
                binding.vpPhoto.hidden()
            } else {
                binding.imgPlaceHolder.hidden()
                binding.vpPhoto.display()
                photos.forEach { data ->
                    photo.add(data.photo_reference)
                }
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
                val imageView: AppCompatImageView =
                    sliderIndicators.getChildAt(i) as AppCompatImageView
                if (i == position)
                    imageView.setImageDrawable(applicationContext.getDrawableCompat(R.drawable.background_slider_indicator_active))
                else
                    imageView.setImageDrawable(applicationContext.getDrawableCompat(R.drawable.background_slider_indicator_inactive))
            }
        }
    }
}