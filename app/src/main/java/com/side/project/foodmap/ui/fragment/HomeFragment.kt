package com.side.project.foodmap.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.android.gms.maps.model.LatLng
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.Location
import com.side.project.foodmap.data.remote.api.restaurant.DrawCardRes
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.DialogPromptSelectBinding
import com.side.project.foodmap.databinding.DialogSearchBinding
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.activity.ListActivity
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.adapter.PopularSearchAdapter
import com.side.project.foodmap.ui.adapter.RegionSelectAdapter
import com.side.project.foodmap.ui.adapter.SearchAndHistoryAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants.DISTANCE
import com.side.project.foodmap.util.Constants.IS_NEAR_SEARCH
import com.side.project.foodmap.util.Constants.KEYWORD
import com.side.project.foodmap.util.Constants.LATITUDE
import com.side.project.foodmap.util.Constants.LONGITUDE
import com.side.project.foodmap.util.Constants.PLACE_ID
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.CoilEngine
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.tools.Method.getDistance
import com.side.project.foodmap.util.tools.Method.logE
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.math.abs

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModel()

    private lateinit var regionList: ArrayList<String>
    private lateinit var region: String
    private lateinit var placeId: String
    private var regionID: Int = 0
    private var keyword: String = ""

    private var startY = 0

    private var isRecentPopularSearch: Boolean = true

    private lateinit var searchAndHistoryAdapter: SearchAndHistoryAdapter
    private lateinit var popularSearchAdapter: PopularSearchAdapter

    private lateinit var oldLatLng: Location

    init {
        Method.getFcmToken { token -> viewModel.putFcmToken(token) }
    }

    private val openGps =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result?.let {
                try {
                    mActivity.initLocationService()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    override fun FragmentHomeBinding.initialize() {
        mActivity.initLocationService()

        if (!mActivity.checkDeviceGPS() || !mActivity.checkNetworkGPS())
            viewModel.putUserRegion(getString(R.string.text_taipei))

        binding.paddingTop = mActivity.getStatusBarHeight()

        binding.vm = viewModel
        binding.isPopularSearch = isRecentPopularSearch
        regionList = ArrayList(listOf(*resources.getStringArray(R.array.search_type)))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog.showLoadingDialog(mActivity, false)
        view.delayOnLifecycle(1000L) {
            // 為了取的第一次的經緯度
            doInitialize()
            setListener()
        }
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 傳送 FCM Token
                launch {
                    viewModel.putFcmTokenState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                logE("FCM Put", "Loading")
                            }
                            is Resource.Success -> {
                                logE("FCM Put", "Success")
                            }
                            is Resource.Error -> {
                                logE("FCM Put", "Error:${it.message.toString()}")
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 取得使用者區域設定
                launch {
                    viewModel.userRegion.collect { region ->
                        dialog.cancelAllDialog()
                        this@HomeFragment.region = region
                        regionID = regionList.indexOf(region)
                        viewModel.nearSearch(
                            region,
                            LatLng(mActivity.myLatitude, mActivity.myLongitude)
                        )
                        viewModel.popularSearch(
                            region, LatLng(mActivity.myLatitude, mActivity.myLongitude),
                            if (isRecentPopularSearch) 0 else 1
                        )
                    }
                }
                // 人氣餐廳
                launch {
                    viewModel.popularSearchState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Popular Search", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                                binding.vpPopular.hidden()
                                binding.lottieNoData.display()
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Popular Search", "Success")
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Popular Search", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                viewModel.getDrawCardData()
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 人氣餐廳 From Room
                launch {
                    viewModel.getDrawCard.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Popular Search Room", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Popular Search Room", "Success")
                                dialog.cancelLoadingDialog()
                                binding.vpPopular.display()
                                binding.lottieNoData.hidden()
                                resource.data?.let { data ->
                                    if (data.result.msg.isNullOrEmpty() && data.result.placeList.isNotEmpty())
                                        initPopularCard(data)
                                    else {
                                        binding.vpPopular.hidden()
                                        binding.lottieNoData.display()
                                    }
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Popular Search Room", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                binding.vpPopular.hidden()
                                binding.lottieNoData.display()
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 附近搜尋
                launch {
                    viewModel.nearSearchState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Near Search", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Near Search", "Success")
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Near Search", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                viewModel.getDistanceSearchData()
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 附近搜尋 From Room
                launch {
                    viewModel.getDistanceSearch.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Near Search Room", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Near Search Room", "Success")
                                dialog.cancelLoadingDialog()
                                resource.data?.let { data ->
                                    binding.nearSearch = data
                                    placeId = data.result.placeList[0].place_id
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Near Search Room", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 自動更新
                launch {
                    mActivity.locationGet.observe(viewLifecycleOwner) { location ->
                        if (!::oldLatLng.isInitialized) {
                            oldLatLng = location
                            return@observe
                        }
                        if (getDistance(
                                LatLng(location.lat, location.lng),
                                LatLng(oldLatLng.lat, oldLatLng.lng)
                            ) * 1000 > 100
                        ) {
                            oldLatLng = location
                            if (::region.isInitialized) {
                                viewModel.nearSearch(
                                    region,
                                    LatLng(mActivity.myLatitude, mActivity.myLongitude)
                                )
                                viewModel.popularSearch(
                                    region, LatLng(mActivity.myLatitude, mActivity.myLongitude),
                                    if (isRecentPopularSearch) 0 else 1
                                )
                            }
                        }
                    }
                }
                // 搜尋結果
                launch {
                    viewModel.autoCompleteState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Search Result", "Loading")
//                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Search Result", "Success")
//                                dialog.cancelLoadingDialog()
                                resource.data?.let { data ->
                                    if (::searchAndHistoryAdapter.isInitialized)
                                        searchAndHistoryAdapter.setData(false, keyword, data)
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Search Result", "Error:${resource.message.toString()}")
//                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 歷史紀錄
                launch {
                    viewModel.historySearchList.observe(viewLifecycleOwner) { historySearchList ->
                        if (::searchAndHistoryAdapter.isInitialized && keyword.isEmpty())
                            searchAndHistoryAdapter.setData(
                                true,
                                keyword,
                                historySearchList.reversed()
                            )
                    }
                }
            }
        }
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding.run {
            tvCategory.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    displayRegionDialog()
                }
            }

            imgUserPicture.setOnClickListener {
                (mActivity as MainActivity).switchFragment(R.id.profilesFragment)
            }

            searchBar.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    displaySearchDialog()
                }
            }

            imgCameraSearch.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    pictureSelector()
                }
            }

            imgSoundSearch.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    displaySpeechRecognizer()
                }
            }

            tvPopular.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    isRecentPopularSearch = !isRecentPopularSearch
                    togglePopularSearch(isRecentPopularSearch)
                    binding.imgPopularBack.gone()
                    binding.imgPopularForward.display()
                }
            }

            imgPopularForward.setOnClickListener {
                vpPopular.currentItem += 1
            }

            imgPopularBack.setOnClickListener {
                vpPopular.currentItem -= 1
            }

            imgRefresh.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    if (::region.isInitialized) {
                        viewModel.nearSearch(
                            region,
                            LatLng(mActivity.myLatitude, mActivity.myLongitude)
                        )
                        viewModel.popularSearch(
                            region, LatLng(mActivity.myLatitude, mActivity.myLongitude),
                            if (isRecentPopularSearch) 0 else 1
                        )
                    }
                }
            }

            cardAllRestaurant.setOnClickListener {
                watchDetail(placeId)
            }

            tvViewMore.setOnClickListener {
                if (!mActivity.checkDeviceGPS() || !mActivity.checkNetworkGPS()) {
                    displayNotGpsDialog()
                    return@setOnClickListener
                }
                Bundle().also { b ->
                    val latLng: LatLng = Method.getCurrentLatLng(
                        region,
                        LatLng(mActivity.myLatitude, mActivity.myLongitude)
                    )
                    b.putString(KEYWORD, region)
                    b.putBoolean(IS_NEAR_SEARCH, true)
                    b.putDouble(LATITUDE, latLng.latitude)
                    b.putDouble(LONGITUDE, latLng.longitude)
                    mActivity.start(ListActivity::class.java, b)
                }
            }

            // 暫保留
//            vpPopular.setOnTouchListener { _, event ->
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN ->
//                        // 手指按下
//                        // 記錄當前的Y坐標
//                        startY = event.y.toInt()
//                    MotionEvent.ACTION_MOVE -> {
//                        // 手指移動
//                        val endY = event.y
//                        val distanceY = endY - startY
//                        // 如果手指向下滑動
//                        scrollView.isNestedScrollingEnabled = distanceY <= 0
//                    }
//                    MotionEvent.ACTION_UP ->
//                        // 手指抬起
//                        // 允許NestedScrollView滾動
//                        scrollView.isNestedScrollingEnabled = true
//                }
//                false
//            }
        }
    }

    private fun togglePopularSearch(isRecentPopularSearch: Boolean) {
        binding.isPopularSearch = isRecentPopularSearch
        if (isRecentPopularSearch)
            viewModel.popularSearch(
                region,
                LatLng(mActivity.myLatitude, mActivity.myLongitude),
                mode = 0
            )
        else
            viewModel.popularSearch(
                region,
                LatLng(mActivity.myLatitude, mActivity.myLongitude),
                mode = 1
            )
    }

    private fun displayRegionDialog() {
        val dialogBinding = DialogPromptSelectBinding.inflate(layoutInflater)
        val regionSelectAdapter = RegionSelectAdapter()
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                // initialize
                titleText = getString(R.string.hint_select_region)
                hideCancel = true
                hideConfirm = true
                listItem.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = regionSelectAdapter
                }
                regionSelectAdapter.setRegionList(regionList, regionID)
                // auto scroll to top
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }
                smoothScroller.targetPosition = regionID
                listItem.layoutManager?.startSmoothScroll(smoothScroller)
                // listener
                regionSelectAdapter.onItemClick = { region ->
                    if (!mActivity.checkDeviceGPS() || !mActivity.checkNetworkGPS() && region == getString(
                            R.string.hint_near_region
                        ))
                        displayNotGpsDialog()
                    else if (region != regionList[regionID]) {
                        mActivity.initLocationService()
                        viewModel.putUserRegion(region)
                        dialog.showLoadingDialog(mActivity, false)
                    } else
                        mActivity.initLocationService()
                }
            }
        }
    }

    private fun initPopularCard(drawCardRes: DrawCardRes) {
        popularSearchAdapter = PopularSearchAdapter()
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + (r * 0.15f)
            }
        }
        binding.vpPopular.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(compositePageTransformer)
            adapter = popularSearchAdapter
            if (drawCardRes.result.placeList.size > 0) {
                popularSearchAdapter.setData(drawCardRes.result.placeList)
                popularSearchAdapter.setMyLocation(
                    LatLng(
                        mActivity.myLatitude,
                        mActivity.myLongitude
                    )
                )
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (popularSearchAdapter.getDataSize() == 1) {
                        binding.imgPopularBack.gone()
                        binding.imgPopularForward.gone()
                    }
                    when (currentItem) {
                        0 -> {
                            binding.imgPopularBack.gone()
                            if (popularSearchAdapter.getDataSize() == 2)
                                binding.imgPopularForward.display()
                        }
                        popularSearchAdapter.getDataSize() - 1 -> {
                            binding.imgPopularForward.gone()
                            if (popularSearchAdapter.getDataSize() == 2)
                                binding.imgPopularBack.display()
                        }
                        else -> {
                            binding.imgPopularBack.display()
                            binding.imgPopularForward.display()
                        }
                    }
                }
            })
        }

        popularSearchAdapter.onItemFavoriteClick = { placeId, isFavorite ->
            if (isFavorite) {
                viewModel.quickPullFavorite(arrayListOf(placeId))
                false
            } else {
                viewModel.quickPushFavorite(arrayListOf(placeId))
                true
            }
        }

        popularSearchAdapter.onItemClick = { placeId ->
            watchDetail(placeId)
        }
    }

    private fun watchDetail(placeId: String) {
        if (placeId.isEmpty()) return
        try {
            logE("Watch Detail", "Success")
            Bundle().also { b ->
                b.putString(PLACE_ID, placeId)
                mActivity.start(DetailActivity::class.java, b)
            }
        } catch (e: Exception) {
            logE("Watch Detail", "Error")
            requireActivity().displayShortToast(getString(R.string.hint_error))
        }
    }

    private fun displayNotGpsDialog() {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    hideCancel = true
                    imgPromptIcon.setImageResource(R.drawable.ic_public)
                    titleText = getString(R.string.hint_prompt_not_gps_title)
                    tvConfirm.setOnClickListener {
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                            openGps.launch(this)
                        }
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }

    private val getTextFromSpeechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val getText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                    it[0]
                }.toString()
                displaySearchDialog(getText)
            } catch (e: Exception) {
                e.printStackTrace()
                mActivity.displayShortToast(getString(R.string.hint_error))
            }
        }
    }

    private fun displaySpeechRecognizer() {
        if (!requestAudioPermission())
            return
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also { i ->
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            getTextFromSpeechRecognizer.launch(i)
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent: Uri = result.uriContent as Uri
            val chinese = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            val image = InputImage.fromFilePath(requireContext(), uriContent)
            chinese.process(image)
                .addOnSuccessListener { getText ->
                    logE("Text Recognition", "Success")
                    displaySearchDialog(getText.text)
                }
                .addOnFailureListener { e ->
                    logE("Text Recognition", "Error:${e.message.toString()}")
                }
        } else
            pictureSelector()
    }

    private fun pictureSelector() {
        if (!requestCameraPermission())
            return
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setMaxSelectNum(1)
            .setCameraImageFormat(PictureMimeType.PNG)
            .setImageEngine(CoilEngine())
            .setImageSpanCount(3)
            .setSelectionMode(SelectModeConfig.SINGLE)
            .setRecyclerAnimationMode(R.anim.layout_animation_random)
            .isPageStrategy(true)
            .isCameraRotateImage(true)
            .isGif(false)
            .isFastSlidingSelect(true)
            .isDirectReturnSingle(true)
            .forResult(object: OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    val imageFile = File(result?.first()?.realPath ?: "")
                    try {
                        cropImage.launch(
                            CropImageContractOptions(
                                uri = imageFile.toUri(),
                                cropImageOptions = CropImageOptions(
                                    initialCropWindowPaddingRatio = 0f
                                )
                            )
                        )
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }

                override fun onCancel() {
                    mActivity.displayShortToast(getString(R.string.hint_crop_cancel))
                }
            })
    }

    private fun displaySearchDialog(setText: String = "") {
        val dialogBinding = DialogSearchBinding.inflate(layoutInflater)
        var timer: Timer? = null
        dialog.showBottomDialog(mActivity, dialogBinding, true).let {
            dialogBinding.run {
                var radius: Long = 1000
                distance = " 1"
                initSearchRv(dialogBinding)
                keyword = ""
                isHistory = true
                viewModel.getHistorySearchData()

                seekBarRange.max = (30 - 1) / 1 // (MAX - MIN) / STEP
                seekBarRange.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        val value = 1 + p1 * 1 // MIN + VALUE * STEP
                        distance = if (value < 10) " $value" else "$value"
                        radius = (value * 1000).toLong()
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        keyword = edSearch.text.toString().trim()
                        if (keyword.isNotEmpty()) {
                            timer = Timer()
                            timer?.schedule(object : TimerTask() {
                                override fun run() {
                                    Handler(Looper.getMainLooper()).post {
                                        isHistory = false
                                        viewModel.autoComplete(
                                            input = keyword,
                                            region = region,
                                            latLng = LatLng(
                                                mActivity.myLatitude,
                                                mActivity.myLongitude
                                            ),
                                            radius = radius
                                        )

                                    }
                                }
                            }, 500)
                        } else {
                            isHistory = true
                            viewModel.getHistorySearchData()
                        }
                    }
                })

                imgBack.setOnClickListener { dialog.cancelBottomDialog() }

                if (setText != "") {
                    keyword = setText
                    edSearch.setText(setText)
                    imgCameraSearch.gone()
                    imgSoundSearch.gone()
                    imgClear.display()
                    timer = Timer()
                    timer?.schedule(object : TimerTask() {
                        override fun run() {
                            Handler(Looper.getMainLooper()).post {
                                isHistory = false
                                viewModel.autoComplete(
                                    input = setText,
                                    region = region,
                                    latLng = LatLng(
                                        mActivity.myLatitude,
                                        mActivity.myLongitude
                                    ),
                                    radius = radius
                                )

                            }
                        }
                    }, 500)
                }

                imgCameraSearch.setOnClickListener { pictureSelector() }

                imgSoundSearch.setOnClickListener { displaySpeechRecognizer() }

                tvClear.setOnClickListener {
                    viewModel.deleteAllHistoryData()
                    viewModel.getHistorySearchData()
                }

                edSearch.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        timer?.cancel()
                    }

                    override fun afterTextChanged(editable: Editable?) {
                        keyword = editable.toString().trim()
                        if (keyword.isNotEmpty()) {
                            timer = Timer()
                            timer?.schedule(object : TimerTask() {
                                override fun run() {
                                    Coroutines.main {
                                        isHistory = false
                                        viewModel.autoComplete(
                                            input = keyword,
                                            region = region,
                                            latLng = LatLng(
                                                mActivity.myLatitude,
                                                mActivity.myLongitude
                                            ),
                                            radius = radius
                                        )
                                    }
                                }
                            }, 500)
                        } else {
                            isHistory = true
                            viewModel.getHistorySearchData()
                        }
                    }
                })
            }
        }
    }

    private fun initSearchRv(dialogBinding: DialogSearchBinding) {
        searchAndHistoryAdapter = SearchAndHistoryAdapter()
        dialogBinding.rvResultAndHistory.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = searchAndHistoryAdapter

            searchAndHistoryAdapter.onItemClick = { historySearch ->
                viewModel.insertHistoryData(historySearch)
                if (historySearch.place_id != "")
                    watchDetail(historySearch.place_id)
                else
                    Bundle().also { b ->
                        val latLng: LatLng = Method.getCurrentLatLng(
                            keyword,
                            LatLng(mActivity.myLatitude, mActivity.myLongitude)
                        )
                        b.putString(KEYWORD, historySearch.name)
                        b.putInt(DISTANCE, (dialogBinding.seekBarRange.progress + 1) * 1000)
                        b.putBoolean(IS_NEAR_SEARCH, false)
                        b.putDouble(LATITUDE, latLng.latitude)
                        b.putDouble(LONGITUDE, latLng.longitude)
                        mActivity.start(ListActivity::class.java, b)
                    }
            }

            searchAndHistoryAdapter.onItemLongClick = { view, historySearch ->
                val popupMenu = PopupMenu(requireContext(), view)
                popupMenu.menuInflater.inflate(R.menu.item_history_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_delete -> {
                            viewModel.deleteHistoryData(historySearch)
                            viewModel.getHistorySearchData()
                        }
                    }
                    true
                }
                popupMenu.show()
            }
        }
    }
}