package com.side.project.foodmap.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.*
import com.side.project.foodmap.data.remote.restaurant.DetailsByPlaceIdRes
import com.side.project.foodmap.databinding.ActivityDetailBinding
import com.side.project.foodmap.databinding.DialogNavigationModeBinding
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.adapter.DetailPhotoAdapter
import com.side.project.foodmap.ui.adapter.GoogleReviewsAdapter
import com.side.project.foodmap.ui.adapter.WorkDayAdapter
import com.side.project.foodmap.ui.fragment.other.AlbumFragment
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.DetailViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Constants.IS_BLACK_LIST
import com.side.project.foodmap.util.Constants.IS_FAVORITE
import com.side.project.foodmap.util.Constants.PLACE_ID
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.animPolyline.AnimatedPolyline
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: DetailViewModel
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var mLoc: LatLng
    private lateinit var animatedPolyline: AnimatedPolyline

    // Data
    private lateinit var placesDetails: DetailsByPlaceIdRes.Result
    private lateinit var placeId: String

    // Wait push favorite list to Database
    private lateinit var favoriteList: FavoriteList

    // Tool
    private var checkFavorite: Boolean = false
    private var checkBlackList: Boolean = false
    private lateinit var detailPhotoAdapter: DetailPhotoAdapter
    private lateinit var googleReviewsAdapter: GoogleReviewsAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]
        binding.paddingTop = getStatusBarHeight()

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        checkNetWork { onBackPressed() }

        getArguments()
        initLayoutOption()
        initLocationService()
        doInitialize()
        setListener()
    }

    override fun onBackPressed() {
        Intent().apply {
            putExtra(PLACE_ID, placeId)
            putExtra(IS_FAVORITE, checkFavorite)
            putExtra(IS_BLACK_LIST, checkBlackList)
            setResult(RESULT_OK, this)
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        map.clear()
        super.onDestroy()
    }

    private fun getArguments() {
        intent.extras?.let {
            placeId = it.getString(PLACE_ID, "") ?: ""
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()

        map.setOnCameraMoveListener {
            map.cameraPosition.target.let {
                mLoc = it
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initGoogleMap() {
        if (!mActivity.requestLocationPermission() || !::map.isInitialized)
            return
        setMyLocation()
        map.apply {
            uiSettings.setAllGesturesEnabled(true)
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isCompassEnabled = true
            setCompass()
        }
    }

    private fun setMyLocation() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!::map.isInitialized) return@launch
            mLoc = LatLng(mActivity.myLatitude, mActivity.myLongitude)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        mActivity.myLatitude,
                        mActivity.myLongitude
                    ), DEFAULT_ZOOM
                )
            )
        }
    }

    private fun initLayoutOption() {
        binding.apply {
            bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutOption.layoutDetail)
            with(bottomSheetBehavior) {
                skipCollapsed = false
                isFitToContents = false
                halfExpandedRatio = 0.7F
                state = BottomSheetBehavior.STATE_HALF_EXPANDED
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                isFitToContents = true
                                layoutOption.scrollView.post {
                                    layoutOption.scrollView.apply {
                                        fling(0)
                                        scrollTo(0, 0)
                                        bottomSheetBehavior.state =
                                            BottomSheetBehavior.STATE_COLLAPSED
                                    }
                                }
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> isFitToContents = true
                            else -> Unit
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        val h = bottomSheet.height.toFloat()
                        val off = h * slideOffset

                        mLoc.let {
                            when (state) {
                                BottomSheetBehavior.STATE_DRAGGING -> {
                                    setMapPaddingBottom(off)
                                    //reposition marker at the center
                                    map.moveCamera(CameraUpdateFactory.newLatLng(mLoc))
                                }
                                BottomSheetBehavior.STATE_SETTLING -> {
                                    setMapPaddingBottom(off)
                                    //reposition marker at the center
                                    map.moveCamera(CameraUpdateFactory.newLatLng(mLoc))
                                }
                                else -> Unit
                            }
                        }
                    }
                })
            }
        }
    }

    private fun setMapPaddingBottom(offset: Float) {
        //From 0.0 (min) - 1.0 (max) // bsExpanded - bsCollapsed;
        val maxMapPaddingBottom = 1.0f
        map.setPadding(0, 0, 0, (offset * maxMapPaddingBottom).roundToInt())
    }

    private fun doInitialize() {
        if (::placeId.isInitialized)
            viewModel.searchDetail(placeId)

        checkFavorite = false
        binding.layoutOption.isFavorite = checkFavorite

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 搜尋詳細資料
                launch {
                    viewModel.placeDetailFlow.collect {
                        when (it) {
                            is Resource.Loading -> dialog.showLoadingDialog(mActivity, false)
                            is Resource.Success -> {
                                it.data?.result?.let { data ->
                                    placesDetails = data
                                    setupData(data)
                                }
                                dialog.cancelLoadingDialog()
                            }
                            is Resource.Error -> {
                                dialog.cancelLoadingDialog()
                                mActivity.displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 取得路線
                launch {
                    viewModel.getRoutePolylineFlow.collect {
                        when (it) {
                            is Resource.Success -> it.data?.result?.let { data ->
                                viewModel.decodePolylineArray = Method.decodePolyline(data.polyline)
                                doMarketPolyLine()
                            }
                            is Resource.Error -> mActivity.displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
                // 加入黑名單
                launch {
                    viewModel.pushBlackListFlow.collect {
                        when (it) {
                            is Resource.Success -> {
                                mActivity.displayShortToast(getString(R.string.hint_success_push_black_list))
                                checkBlackList = true
                            }
                            is Resource.Error -> {
                                mActivity.displayShortToast(getString(R.string.hint_failed_push_black_list))
                                checkBlackList = false
                            }
                            else -> Unit
                        }
                    }
                }
                // 刪除黑名單
                launch {
                    viewModel.pullBlackListFlow.collect {
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
                    viewModel.pushFavoriteListFlow.collect {
                        when (it) {
                            is Resource.Success -> {
                                Method.logE("Push Favorite", "Success")
                                mActivity.displayShortToast(getString(R.string.hint_success_push_favorite))
                                viewModel.insertFavoriteData(favoriteList)
                                checkFavorite = true
                                binding.layoutOption.isFavorite = checkFavorite
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
                    viewModel.pullFavoriteListFlow.collect {
                        when (it) {
                            is Resource.Success -> {
                                Method.logE("Pull Favorite", "Success")
                                mActivity.displayShortToast(getString(R.string.hint_success_pull_favorite))
                                checkFavorite = false
                                binding.layoutOption.isFavorite = checkFavorite
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
        binding.layoutOption.apply {
            doGetPolyline(data.place)
            setupToolButton(data)
            detail = data
            checkFavorite = placesDetails.isFavorite
            checkBlackList = placesDetails.isBlackList
            isFavorite = checkFavorite
            data.place.reviews?.let { reviewsList -> initRvReviews(reviewsList) }
            data.place.photos.let { photoList -> initPhotoSlider(photoList) }
        }
    }

    private fun doGetPolyline(targetInfo: Place) {
        if (!checkMyDeviceGPS())
            return
        viewModel.getPolyLine(
            origin = SetLocation(
                lat = myLatitude,
                lng = myLongitude,
                place_id = ""
            ),
            destination = SetLocation(
                targetInfo.location.lat,
                targetInfo.location.lng,
                targetInfo.place_id
            )
        )
    }

    private fun setCompass() {
        try {
            mapFragment.view?.let { mapView ->
                mapView.findViewWithTag<View>("GoogleMapMyLocationButton").parent?.let { parent ->
                    val vg: ViewGroup = parent as ViewGroup
                    vg.post {
                        val mapCompass: View = parent.getChildAt(4)
                        val rlp = RelativeLayout.LayoutParams(mapCompass.height, mapCompass.height)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)

                        val topMargin = (8 * Resources.getSystem().displayMetrics.density).toInt()
                        val endMargin = (150 * Resources.getSystem().displayMetrics.density).toInt()
                        rlp.setMargins(0, topMargin, endMargin, 0)
                        mapCompass.layoutParams = rlp
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun doMarketPolyLine() {
        lifecycleScope.launch(Dispatchers.Main) {
            placesDetails.place.apply {
                if (!::map.isInitialized) return@launch
                // Marker
                val markerOption = MarkerOptions().apply {
                    position(LatLng(location.lat, location.lng))
                    title(name)
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.img_location))
                }
                map.addMarker(markerOption)
                // PolyLine
                animatedPolyline = AnimatedPolyline(
                    map = map,
                    points = viewModel.decodePolylineArray,
                    polylineOptions = PolylineOptions()
                        .width(24f)
                        .color(getColorCompat(R.color.google_red))
                        .geodesic(true)
                        .pattern(
                            listOf(
                                Dot(), Gap(20f)
                            )
                        ),
                    duration = 1500,
                    interpolator = DecelerateInterpolator(),
                    animatorListenerAdapter = object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            animatedPolyline.start()
                        }
                    }
                )
                animatedPolyline.start()
                // Set Zoom
                setZoomMap()
            }
        }
    }

    private fun setZoomMap() {
        val builder = LatLngBounds.Builder()
        viewModel.decodePolylineArray.forEach { builder.include(it) }
        val bounds = builder.build()
        val metrics = resources.displayMetrics
        val cu = CameraUpdateFactory.newLatLngBounds(
            bounds,
            (metrics.widthPixels * 2.2).toInt(),
            metrics.heightPixels,
            (metrics.widthPixels * 0.7).toInt()
        )
        map.animateCamera(cu)
    }

    private fun setupToolButton(data: DetailsByPlaceIdRes.Result) {
        binding.layoutOption.apply {
            if (data.place.website.isNullOrEmpty()) {
                btnWebsite.background = getDrawableCompat(R.drawable.background_google_gray_button)
                btnWebsite.isClickable = false
            }
            if (data.place.phone.isNullOrEmpty()) {
                btnPhone.background = getDrawableCompat(R.drawable.background_google_gray_button)
                btnPhone.isClickable = false
            }
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

            imgMyLocation.setOnClickListener {
                if (!::map.isInitialized || !::animatedPolyline.isInitialized) return@setOnClickListener
                setMyLocation()
                layoutOption.scrollView.post {
                    layoutOption.scrollView.apply {
                        fling(0)
                        scrollTo(0, 0)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }

            imgMyRoute.setOnClickListener {
                if (!::map.isInitialized || !::animatedPolyline.isInitialized) return@setOnClickListener
                setZoomMap()
                layoutOption.scrollView.post {
                    layoutOption.scrollView.apply {
                        fling(0)
                        scrollTo(0, 0)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }
        }

        binding.layoutOption.run {
            tvTime.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::placesDetails.isInitialized && placesDetails.place.opening_hours.weekday_text == emptyList<String>()) {
                        mActivity.displayShortToast(getString(R.string.text_null))
                        return@setAnimClick
                    }
                    displayRegionDialog()
                }
            }

            tvName.setOnClickListener {
                if (::placesDetails.isInitialized) {
                    Intent(Intent.ACTION_VIEW).also { i ->
                        i.data = Uri.parse(placesDetails.place.url)
                        startActivity(i)
                    }
                } else
                    mActivity.displayShortToast(getString(R.string.hint_no_website))
            }

            tvGoogle.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::placesDetails.isInitialized) {
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
                    if (::placesDetails.isInitialized) {
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
                    if (::placesDetails.isInitialized)
                        displayNavigationModeDialog()
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
                            workDay = placesDetails.place.opening_hours.weekday_text,
                            dine_in = placesDetails.place.dine_in ?: false,
                            takeout = placesDetails.place.takeout ?: false,
                            delivery = placesDetails.place.delivery ?: false,
                            website = placesDetails.place.website ?: "",
                            phone = placesDetails.place.phone ?: "",
                            rating = placesDetails.place.rating ?: 0F,
                            ratings_total = placesDetails.place.ratings_total ?: 0,
                            price_level = placesDetails.place.price_level ?: 0,
                            location = Location(
                                placesDetails.place.location.lat,
                                placesDetails.place.location.lng
                            ),
                            url = placesDetails.place.url ?: ""
                        )
                        viewModel.pushFavorite(arrayListOf(placeId))
                    }
                }
            }

            btnPhone.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::placesDetails.isInitialized) {
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

    private fun displayNavigationModeDialog() {
        // d:開車, b:單車, l:機車, w:步行
        val dialogBinding = DialogNavigationModeBinding.inflate(layoutInflater)
        dialog.showBottomDialog(mActivity, dialogBinding, true).let {
            dialogBinding.run {
                cardDrive.setOnClickListener {
                    goNavigation("d")
                    dialog.cancelBottomDialog()
                }
                cardBike.setOnClickListener {
                    goNavigation("b")
                    dialog.cancelBottomDialog()
                }
                cardMotorcycle.setOnClickListener {
                    goNavigation("l")
                    dialog.cancelBottomDialog()
                }
                cardWalk.setOnClickListener {
                    goNavigation("w")
                    dialog.cancelBottomDialog()
                }
                cardCancel.setOnClickListener { dialog.cancelBottomDialog() }
            }
        }
    }

    private fun goNavigation(mode: String) {
        Intent(Intent.ACTION_VIEW).also { i ->
            i.data = Uri.parse(
                "google.navigation:q=" +
                        "${placesDetails.place.location.lat},${placesDetails.place.location.lng}" +
                        "&mode=$mode"
            )
            i.`package` = "com.google.android.apps.maps"
            i.resolveActivity(applicationContext.packageManager)?.let {
                startActivity(i)
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
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = workDayAdapter
                    placesDetails.place.opening_hours.weekday_text.let {
                        workDayAdapter.submitList(it.toMutableList())
                    }
                }
            }
        }
    }

    private fun initRvReviews(review: List<Review>) {
        googleReviewsAdapter = GoogleReviewsAdapter()
        binding.layoutOption.rvReviews.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = googleReviewsAdapter
            googleReviewsAdapter.submitList(review.toMutableList())
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
        binding.layoutOption.vpPhoto.apply {
            offscreenPageLimit = 1
            adapter = detailPhotoAdapter
            detailPhotoAdapter.submitList(photoIdList.toMutableList()) {
                if (photoIdList.isEmpty()) {
                    binding.layoutOption.apply {
                        vpPhoto.animation = animManager.fromTop
                        vpPhoto.gone()
                        imgProgress.gone()
                    }
                }
            }

            setupSliderIndicators(photoIdList.size)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setCurrentSliderIndicator(position)
                }
            })
        }

        detailPhotoAdapter.onItemClick = { position ->
            Bundle().also {
                val type = object : TypeToken<List<String>>() {}.type
                it.putString(
                    Constants.ALBUM_IMAGE_RESOURCE,
                    Gson().toJson(detailPhotoAdapter.currentList, type)
                )
                it.putInt(Constants.IMAGE_POSITION, position)

                val ft = mActivity.supportFragmentManager.beginTransaction()
                val albumDialog = AlbumFragment()
                albumDialog.arguments = it

                val prevDialog =
                    mActivity.supportFragmentManager.findFragmentByTag(Constants.DIALOG_ALBUM)
                if (prevDialog != null)
                    ft.remove(prevDialog)

                albumDialog.show(ft, Constants.DIALOG_ALBUM)
                albumDialog.onDismissListener = {}
            }
        }
    }

    private fun setupSliderIndicators(count: Int) {
        binding.layoutOption.run {
            val indicators: Array<AppCompatImageView?> = arrayOfNulls(count)
            val layoutParams: LinearLayoutCompat.LayoutParams = LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(10, 0, 10, 0)

            indicators.forEachIndexed { id, _ ->
                indicators[id] = AppCompatImageView(applicationContext)
                indicators[id]?.setImageDrawable(getDrawableCompat(R.drawable.background_slider_indicator_inactive))

                indicators[id]?.layoutParams = layoutParams
                sliderIndicators.addView(indicators[id])
            }
        }
    }

    private fun setCurrentSliderIndicator(position: Int) {
        binding.layoutOption.run {
            val childCount: Int = sliderIndicators.childCount
            for (i in 0 until childCount) {
                val imageView: AppCompatImageView =
                    sliderIndicators.getChildAt(i) as AppCompatImageView
                if (i == position)
                    imageView.setImageDrawable(getDrawableCompat(R.drawable.background_slider_indicator_active))
                else
                    imageView.setImageDrawable(getDrawableCompat(R.drawable.background_slider_indicator_inactive))
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
                    imgPromptIcon.setImageResource(R.drawable.ic_error)
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

    companion object {
        private const val DEFAULT_ZOOM = 18F
    }
}