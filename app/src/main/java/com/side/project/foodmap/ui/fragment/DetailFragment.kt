package com.side.project.foodmap.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.data.remote.api.Location
import com.side.project.foodmap.data.remote.api.Review
import com.side.project.foodmap.data.remote.api.restaurant.DetailsByPlaceIdRes
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.databinding.FragmentDetailBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.adapter.DetailPhotoAdapter
import com.side.project.foodmap.ui.adapter.GoogleReviewsAdapter
import com.side.project.foodmap.ui.adapter.WorkDayAdapter
import com.side.project.foodmap.ui.fragment.other.AlbumFragment
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.DetailViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DetailFragment : BaseFragment<FragmentDetailBinding>(R.layout.fragment_detail) {
    private val viewModel: DetailViewModel by activityViewModel()

    // Data
    private lateinit var placesDetails: DetailsByPlaceIdRes.Result
    private lateinit var placeId: String
    private var isFirstLoad: Boolean = false

    // Wait push favorite list to Database
    private lateinit var favoriteList: FavoriteList

    // Tool
    private var checkFavorite: Boolean = false
    private var checkBlackList: Boolean = false
    private lateinit var detailPhotoAdapter: DetailPhotoAdapter
    private lateinit var googleReviewsAdapter: GoogleReviewsAdapter

    override fun FragmentDetailBinding.initialize() {
        mActivity.initLocationService()
        binding.paddingTop = mActivity.getStatusBarHeight()
        placeId = (mActivity as DetailActivity).placeId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        if (::placeId.isInitialized && !isFirstLoad)
            viewModel.searchDetail(placeId)

        isFirstLoad = true
        checkFavorite = false
        binding.isFavorite = checkFavorite

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
                                    placesDetails = data
                                    setupData(data)
                                }
                            }
                            is Resource.Error -> {
                                Method.logE("Search Detail", "Error:${it.message.toString()}")
                                dialog.cancelLoadingDialog()
                                mActivity.displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 加入黑名單
                launch {
                    viewModel.pushBlackListState.collect {
                        when (it) {
                            is Resource.Success -> {
                                Method.logE("Push Black List", "Success")
                                mActivity.displayShortToast(getString(R.string.hint_success_push_black_list))
                                checkBlackList = true
//                                onBackPressed()
                            }
                            is Resource.Error -> {
                                Method.logE("Push Black List", "Error:${it.message.toString()}")
                                mActivity.displayShortToast(getString(R.string.hint_failed_push_black_list))
                                checkBlackList = false
                            }
                            else -> Unit
                        }
                    }
                }
                // 刪除黑名單
                launch {
                    viewModel.pullBlackListState.collect {
                        when (it) {
                            is Resource.Success -> {
                                Method.logE("Pull Black List", "Success")
                                mActivity.displayShortToast(getString(R.string.hint_success_pull_black_list))
                                checkBlackList = false
                            }
                            is Resource.Error -> {
                                Method.logE("Pull Black List", "Error:${it.message.toString()}")
                                mActivity.displayShortToast(getString(R.string.hint_failed_pull_black_list))
                                checkBlackList = true
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
                                mActivity.displayShortToast(getString(R.string.hint_success_push_favorite))
                                viewModel.insertFavoriteData(favoriteList)
                                checkFavorite = true
                                binding.isFavorite = checkFavorite
                            }
                            is Resource.Error -> {
                                Method.logE("Push Favorite", "Error:${it.message.toString()}")
                                mActivity.displayShortToast(getString(R.string.hint_failed_push_favorite))
                                checkFavorite = false
                            }
                            else -> Unit
                        }
                    }
                }
                // 刪除最愛
                launch {
                    viewModel.pullFavoriteState.collect {
                        when (it) {
                            is Resource.Success -> {
                                Method.logE("Pull Favorite", "Success")
                                mActivity.displayShortToast(getString(R.string.hint_success_pull_favorite))
                                checkFavorite = false
                                binding.isFavorite = checkFavorite
                            }
                            is Resource.Error -> {
                                Method.logE("Pull Favorite", "Error:${it.message.toString()}")
                                mActivity.displayShortToast(getString(R.string.hint_failed_pull_favorite))
                                checkFavorite = true
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun setupData(data: DetailsByPlaceIdRes.Result) {
        binding.apply {
            setupToolButton(data)
            detail = data
            checkFavorite = placesDetails.isFavorite
            checkBlackList = placesDetails.isBlackList
            isFavorite = checkFavorite
            data.place.reviews?.let { reviewsList -> initRvReviews(reviewsList) }
            data.place.photos?.let { photoList -> initPhotoSlider(photoList) }
        }
    }

    private fun setupToolButton(data: DetailsByPlaceIdRes.Result) {
        binding.apply {
            if (data.place.website.isNullOrEmpty())
                btnWebsite.background = requireContext().getDrawableCompat(R.drawable.background_google_gray_button)
            if (data.place.phone.isNullOrEmpty())
                btnPhone.background = requireContext().getDrawableCompat(R.drawable.background_google_gray_button)
        }
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding.run {
            tvBack.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    mActivity.onBackPressed()
                }
            }

            imgReport.setOnClickListener {
                if (checkBlackList)
                    displayModifyBlackListDialog(false)
                else
                    displayModifyBlackListDialog(true)
            }

            tvTime.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::placesDetails.isInitialized && placesDetails.place.opening_hours.weekday_text == emptyList<String>()) {
                        mActivity.displayShortToast(getString(R.string.text_null))
                        return@setAnimClick
                    }
                    displayRegionDialog()
                }
            }

            tvGoogle.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::placesDetails.isInitialized && placesDetails.place.url != null) {
                        Intent(Intent.ACTION_VIEW).also { i ->
                            i.data = Uri.parse(placesDetails.place.url)
                            startActivity(i)
                        }
                    } else
                        mActivity.displayShortToast(getString(R.string.hint_no_website))
                }
            }

            btnWebsite.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::placesDetails.isInitialized && placesDetails.place.website != null) {
                        Intent(Intent.ACTION_VIEW).also { i ->
                            i.data = Uri.parse(placesDetails.place.website)
                            startActivity(i)
                        }
                    } else
                        mActivity.displayShortToast(getString(R.string.hint_no_website))
                }
            }

            btnNavigation.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    // TODO(導航)
                    if (::placesDetails.isInitialized) {

                    }
                }
            }

            btnFavorite.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (checkFavorite)
                        displayRemoveFavoriteDialog()
                    else {
                        favoriteList = FavoriteList(
                            place_id = placesDetails.place.place_id,
                            photos = placesDetails.place.photos,
                            name = placesDetails.place.name,
                            vicinity = placesDetails.place.vicinity,
                            workDay = placesDetails.place.opening_hours.weekday_text ?: emptyList(),
                            dine_in = placesDetails.place.dine_in ?: false,
                            takeout = placesDetails.place.takeout ?: false,
                            delivery = placesDetails.place.delivery ?: false,
                            website = placesDetails.place.website ?: "",
                            phone = placesDetails.place.phone ?: "",
                            rating = placesDetails.place.rating ?: 0F,
                            ratings_total = placesDetails.place.ratings_total ?: 0,
                            price_level = placesDetails.place.price_level ?: 0,
                            location = Location(placesDetails.place.location.lat, placesDetails.place.location.lng),
                            url = placesDetails.place.url ?: ""
                        )
                        viewModel.pushFavorite(arrayListOf(placeId))
                    }
                }
            }

            btnPhone.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::placesDetails.isInitialized && placesDetails.place.phone != null) {
                        Intent(Intent.ACTION_DIAL).also { i ->
                            i.data = Uri.parse("tel:${placesDetails.place.phone}")
                            startActivity(i)
                        }
                    } else
                        mActivity.displayShortToast(getString(R.string.hint_no_phone))
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
                    placesDetails.place.opening_hours.weekday_text?.let {
                        workDayAdapter.setWorkdayList(it)
                    }
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

    private fun initPhotoSlider(photoIdList: List<String>) {
        detailPhotoAdapter = DetailPhotoAdapter()
        binding.vpPhoto.apply {
            offscreenPageLimit = 1
            adapter = detailPhotoAdapter
            detailPhotoAdapter.setPhotoIdList(photoIdList)
            setupSliderIndicators(photoIdList.size)

            if (photoIdList.isEmpty()) {
                binding.imgPlaceHolder.display()
                binding.vpPhoto.hidden()
            } else {
                binding.imgPlaceHolder.hidden()
                binding.vpPhoto.display()
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentSliderIndicator(position)
                }
            })
        }

        detailPhotoAdapter.onItemClick = { imgView, photos, _, position ->
            Bundle().also {
                val type = object : TypeToken<List<String>>() {}.type
                it.putString(Constants.ALBUM_IMAGE_RESOURCE, Gson().toJson(photos, type))
                it.putInt(Constants.IMAGE_POSITION, position)
//                val extras = FragmentNavigatorExtras(imgView to imgView.transitionName)
//                findNavController().navigate(
//                    R.id.action_detailFragment_to_albumFragment,
//                    it, null, extras
//                )
                val ft = mActivity.supportFragmentManager.beginTransaction()
                val albumDialog = AlbumFragment()
                albumDialog.arguments = it
                val prevDialog = mActivity.supportFragmentManager.findFragmentByTag(Constants.DIALOG_ALBUM)
                if (prevDialog != null)
                    ft.remove(prevDialog)
                albumDialog.show(ft, Constants.DIALOG_ALBUM)
                albumDialog.onDismissListener = {}
            }
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
                indicators[id] = AppCompatImageView(requireContext())
                indicators[id]?.setImageDrawable(requireContext().getDrawableCompat(R.drawable.background_slider_indicator_inactive))

                indicators[id]?.layoutParams = layoutParams
                sliderIndicators.addView(indicators[id])
            }
        }
    }

    private fun setCurrentSliderIndicator(position: Int) {
        binding.run {
            val childCount: Int = sliderIndicators.childCount
            for (i in 0 until childCount) {
                val imageView: AppCompatImageView =
                    sliderIndicators.getChildAt(i) as AppCompatImageView
                if (i == position)
                    imageView.setImageDrawable(requireContext().getDrawableCompat(R.drawable.background_slider_indicator_active))
                else
                    imageView.setImageDrawable(requireContext().getDrawableCompat(R.drawable.background_slider_indicator_inactive))
            }
        }
    }

    private fun displayRemoveFavoriteDialog() {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    imgPromptIcon.setImageResource(R.drawable.ic_favorite)
                    titleText = getString(R.string.hint_prompt_remove_favorite_title)
                    tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                    tvConfirm.setOnClickListener {
                        viewModel.pullFavorite(arrayListOf(placeId))
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }

    private fun displayModifyBlackListDialog(isAdd: Boolean) {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    imgPromptIcon.setImageResource(R.drawable.ic_report)
                    titleText = if (isAdd)
                        getString(R.string.hint_prompt_add_black_list_title)
                    else
                        getString(R.string.hint_prompt_remove_black_list_title)
                    tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                    tvConfirm.setOnClickListener {
                        if (isAdd)
                            viewModel.pushBlackList(arrayListOf(placeId))
                        else
                            viewModel.pullBlackList(arrayListOf(placeId))
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }
}