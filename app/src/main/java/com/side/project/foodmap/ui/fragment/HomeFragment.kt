package com.side.project.foodmap.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import com.side.project.foodmap.data.remote.AutoComplete
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.DialogRegionListBinding
import com.side.project.foodmap.databinding.DialogSearchBinding
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.activity.ListActivity
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.activity.other.GetLocationActivity
import com.side.project.foodmap.ui.adapter.PopularSearchAdapter
import com.side.project.foodmap.ui.adapter.RegionSelectAdapter
import com.side.project.foodmap.ui.adapter.SearchAndHistoryAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Constants.DISTANCE
import com.side.project.foodmap.util.Constants.IS_BLACK_LIST
import com.side.project.foodmap.util.Constants.IS_FAVORITE
import com.side.project.foodmap.util.Constants.KEYWORD
import com.side.project.foodmap.util.Constants.LIST_TYPE
import com.side.project.foodmap.util.Constants.PLACE_ID
import com.side.project.foodmap.util.Constants.REGION_PLACE_ID
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.CoilEngine
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.tools.Method.getDistance
import com.side.project.foodmap.util.tools.Method.logE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.math.abs

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModel()

    private lateinit var mPlaceId: String
    private var keyword: String = ""

    private lateinit var popularSearchAdapter: PopularSearchAdapter
    private lateinit var regionSelectAdapter: RegionSelectAdapter
    private lateinit var searchAndHistoryAdapter: SearchAndHistoryAdapter

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

        binding?.vm = viewModel
        binding?.paddingTop = mActivity.getStatusBarHeight()
        binding?.isPopularSearch = viewModel.isRecentPopularSearch
        dialog.showLoadingDialog(mActivity, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.root?.delayOnLifecycle(1000) {
            viewModel.getUserRegionFromDataStore()
            doInitialize()
            initPopularCard()
            setListener()
        }
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 傳送 FCM Token
                launch {
                    viewModel.putFcmTokenFlow.collect {
                        when (it) {
                            is Resource.Error -> requireActivity().displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
                // 取得使用者區域
                launch {
                    viewModel.userRegion.collect { region ->
                        viewModel.run {
                            regionPosition = 0
                            regionPlaceId = region
                            isUseMyLocation = region == ""
                            if (regionPlaceId.isEmpty() || regionPlaceId == "")
                                binding?.tvCategory?.text = getString(R.string.hint_near_region)
                            getSyncPlaceList(false)
                        }
                    }
                }
                // 取的區域設定列表
                launch {
                    viewModel.syncPlaceListData.observe(viewLifecycleOwner) { myPlaceLists ->
                        myPlaceLists?.let { placeLists ->
                            viewModel.run {
                                if (::regionSelectAdapter.isInitialized)
                                    regionSelectAdapter.submitList(myPlaceLists.toMutableList())
                                viewModel.myPlaceLists.clear()
                                viewModel.myPlaceLists.addAll(myPlaceLists)

                                placeLists.find { it.place_id == regionPlaceId }?.let {
                                    regionPosition = myPlaceLists.indexOf(it)
                                    isUseMyLocation = false
                                    selectLatLng = it.location
                                    if (it.name != "")
                                        binding?.tvCategory?.text = it.name
                                    else
                                        binding?.tvCategory?.text = it.address
                                }

                                if (isSearchPlaceList) return@observe

                                distanceSearch(
                                    if (isUseMyLocation) Location(
                                        mActivity.myLatitude,
                                        mActivity.myLongitude
                                    ) else selectLatLng
                                )
                                drawCard(
                                    if (isUseMyLocation) Location(
                                        mActivity.myLatitude,
                                        mActivity.myLongitude
                                    ) else selectLatLng,
                                    isRecentPopularSearch
                                )
                            }
                        }
                    }
                }
                // 人氣餐廳
                launch {
                    viewModel.drawCardData.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> dialog.showLoadingDialog(mActivity, false)
                            is Resource.Success -> {
                                binding?.apply {
                                    vpPopular.display()
                                    lottieNoData.hidden()
                                    resource.data?.let { data ->
                                        if (data.result.msg.isNullOrEmpty() && data.result.placeList.isNotEmpty() &&
                                            data.result.placeList.size > 0 && ::popularSearchAdapter.isInitialized
                                        ) {
                                            popularSearchAdapter.submitList(data.result.placeList.toMutableList()) {
                                                vpPopular.delayOnLifecycle(100) {
                                                    vpPopular.setCurrentItem(0, false)
                                                }
                                            }
                                            popularSearchAdapter.setMyLocation(
                                                Location(
                                                    mActivity.myLatitude,
                                                    mActivity.myLongitude
                                                )
                                            )

                                            if (data.result.placeList.size > 1) {
                                                imgPopularForward.display()
                                                imgPopularBack.gone()
                                            } else {
                                                imgPopularForward.gone()
                                                imgPopularBack.display()
                                            }

                                        } else {
                                            vpPopular.hidden()
                                            lottieNoData.display()
                                        }
                                    }
                                    delay(1000)
                                    dialog.cancelLoadingDialog()
                                }
                            }
                            is Resource.Error -> {
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                binding?.vpPopular?.hidden()
                                binding?.lottieNoData?.display()
                                dialog.cancelLoadingDialog()
                            }
                            else -> Unit
                        }
                    }
                }
                // 附近搜尋
                launch {
                    viewModel.distanceSearchFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    binding?.nearSearch = data
                                    mPlaceId = data.result.placeList[0].place_id
                                }
                            }
                            is Resource.Error -> requireActivity().displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
                // 自動更新
                launch {
                    mActivity.locationGet.observe(viewLifecycleOwner) { location ->
                        if (!::oldLatLng.isInitialized || viewModel.regionPlaceId == "") {
                            oldLatLng = location
                            return@observe
                        }
                        if (getDistance(location, oldLatLng) * 1000 > 100) {
                            oldLatLng = location
                            viewModel.run {
                                distanceSearch(
                                    if (isUseMyLocation) Location(
                                        mActivity.myLatitude,
                                        mActivity.myLongitude
                                    ) else selectLatLng
                                )
                                drawCard(
                                    if (isUseMyLocation) Location(
                                        mActivity.myLatitude,
                                        mActivity.myLongitude
                                    ) else selectLatLng,
                                    isRecentPopularSearch
                                )
                            }
                        }
                    }
                }
                // 搜尋結果
                launch {
                    viewModel.autoCompleteFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    if (::searchAndHistoryAdapter.isInitialized)
                                        searchAndHistoryAdapter.submitList(data.result.placeList.toMutableList())
                                }
                            }
                            is Resource.Error -> requireActivity().displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
                // 歷史紀錄
                launch {
                    viewModel.historySearchList.observe(viewLifecycleOwner) { historySearchList ->
                        if (::searchAndHistoryAdapter.isInitialized && keyword.isEmpty())
                            searchAndHistoryAdapter.submitList(
                                historySearchList.toMutableList().reversed()
                            )
                    }
                }
                // 刪除區域設定
                launch {
                    viewModel.pullPlaceListFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> viewModel.getSyncPlaceList(true)
                            is Resource.Error -> requireActivity().displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun initPopularCard() {
        popularSearchAdapter = PopularSearchAdapter()
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + (r * 0.15f)
            }
        }
        binding?.vpPopular?.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(compositePageTransformer)
            adapter = popularSearchAdapter

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (popularSearchAdapter.currentList.size == 1) {
                        binding?.imgPopularBack?.gone()
                        binding?.imgPopularForward?.gone()
                    }
                    when (currentItem) {
                        0 -> {
                            binding?.imgPopularBack?.gone()
                            if (popularSearchAdapter.currentList.size == 2)
                                binding?.imgPopularForward?.display()
                        }
                        popularSearchAdapter.currentList.size - 1 -> {
                            binding?.imgPopularForward?.gone()
                            if (popularSearchAdapter.currentList.size == 2)
                                binding?.imgPopularBack?.display()
                        }
                        else -> {
                            binding?.imgPopularBack?.display()
                            binding?.imgPopularForward?.display()
                        }
                    }
                }
            })
        }

        popularSearchAdapter.onItemFavoriteClick = { placeId, isFavorite ->
            if (isFavorite) {
                viewModel.pullFavorite(arrayListOf(placeId))
                false
            } else {
                viewModel.pushFavorite(arrayListOf(placeId))
                true
            }
        }

        popularSearchAdapter.onItemClick = { placeId ->
            watchDetail(placeId)
        }
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding?.run {
            tvCategory.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    viewModel.getSyncPlaceList(true)
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
                    viewModel.run {
                        initPopularCard()
                        isRecentPopularSearch = !isRecentPopularSearch
                        togglePopularSearch(isRecentPopularSearch)
                    }
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
                    initPopularCard()
                    viewModel.run {
                        distanceSearch(
                            if (isUseMyLocation) Location(
                                mActivity.myLatitude,
                                mActivity.myLongitude
                            ) else selectLatLng
                        )
                        drawCard(
                            if (isUseMyLocation) Location(
                                mActivity.myLatitude,
                                mActivity.myLongitude
                            ) else selectLatLng,
                            isRecentPopularSearch
                        )
                    }
                }
            }

            cardAllRestaurant.setOnClickListener {
                watchDetail(mPlaceId)
            }

            tvViewMore.setOnClickListener {
                if (!mActivity.checkDeviceGPS() && !mActivity.checkNetworkGPS()) {
                    displayNotGpsDialog()
                    return@setOnClickListener
                }
                Bundle().also { b ->
                    viewModel.run {
                        b.putString(LIST_TYPE, Constants.ListType.NEAR_LIST.name)
                        b.putString(KEYWORD, getString(R.string.hint_near_region))
                        b.putInt(DISTANCE, 1000)
                        mActivity.start(ListActivity::class.java, b)
                    }
                }
            }
        }
    }

    private val getNewAddress =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.extras?.let {
                    val regionId = it.getString(REGION_PLACE_ID, "")
                    viewModel.getSyncPlaceList(true)
                    regionSelectAdapter.setSelectRegion(regionId)
                    viewModel.putUserRegion(regionId)
                    viewModel.getUserRegionFromDataStore()
                }
            }
        }

    private fun displayRegionDialog() {
        val dialogBinding = DialogRegionListBinding.inflate(layoutInflater)
        dialog.showBottomDialog(mActivity, dialogBinding, false).let {
            dialogBinding.run {
                viewModel.run {
                    initRvRegion(dialogBinding)
                    tvMyLocation.setOnClickListener {
                        if (regionPlaceId == "") {
                            dialog.cancelBottomDialog()
                            return@setOnClickListener
                        }
                        regionPosition = 0
                        regionPlaceId = ""
                        isUseMyLocation = true
                        selectLatLng = Location(mActivity.myLatitude, mActivity.myLongitude)
                        binding?.tvCategory?.text = getString(R.string.hint_near_region)
                        viewModel.putUserRegion("")
                        viewModel.getUserRegionFromDataStore()
                        dialog.cancelBottomDialog()
                    }

                    tvAddRegion.setOnClickListener {
                        Intent(mActivity, GetLocationActivity::class.java).also {
                            getNewAddress.launch(it)
                            dialog.cancelBottomDialog()
                        }
                    }
                }
            }
        }
    }

    private fun initRvRegion(dialogBinding: DialogRegionListBinding) {
        regionSelectAdapter = RegionSelectAdapter()
        dialogBinding.rvRegion.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = regionSelectAdapter

            if (regionSelectAdapter.currentList.size <= 0)
                regionSelectAdapter.submitList(viewModel.myPlaceLists.toMutableList())

            regionSelectAdapter.setSelectRegion(viewModel.regionPlaceId)

            val smoothScroller: RecyclerView.SmoothScroller =
                object : LinearSmoothScroller(context) {
                    override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                }

            smoothScroller.targetPosition = viewModel.regionPosition
            layoutManager?.startSmoothScroll(smoothScroller)
        }

        regionSelectAdapter.onItemClick = { myPlaceList, _ ->
            myPlaceList.place_id.also {
                if (myPlaceList.place_id == viewModel.regionPlaceId) {
                    dialog.cancelBottomDialog()
                    return@also
                }
                regionSelectAdapter.setSelectRegion(it)
                if (myPlaceList.name != "")
                    binding?.tvCategory?.text = myPlaceList.name
                else
                    binding?.tvCategory?.text = myPlaceList.address
                viewModel.putUserRegion(it)
                viewModel.getUserRegionFromDataStore()
                dialog.cancelBottomDialog()
            }
        }

        regionSelectAdapter.onDeleteClick = { mPlaceList, _ ->
            viewModel.apply {
                pullPlaceList(mPlaceList.place_id)
                deletePlaceListData(mPlaceList)
            }
        }
    }

    private fun togglePopularSearch(isRecentPopularSearch: Boolean) {
        viewModel.run {
            binding?.run {
                isPopularSearch = isRecentPopularSearch
                drawCard(
                    if (isUseMyLocation) Location(
                        mActivity.myLatitude,
                        mActivity.myLongitude
                    ) else selectLatLng,
                    isRecentPopularSearch
                )
            }
        }
    }

    private val toDetail =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.extras?.let { b ->
                    val mPlaceId = b.getString(PLACE_ID, "")
                    val mIsFavorite = b.getBoolean(IS_FAVORITE, false)
                    val mIsBlackList = b.getBoolean(IS_BLACK_LIST, false)
                    val list = popularSearchAdapter.currentList.toMutableList()
                    list.forEachIndexed { index, placeList ->
                        if (placeList.place_id == mPlaceId) {
                            if (mIsBlackList) {
                                list.remove(placeList)
                                popularSearchAdapter.submitList(list)
                                popularSearchAdapter.notifyItemChanged(index)
                                return@registerForActivityResult
                            }
                            if (mIsFavorite) {
                                placeList.isFavorite = mIsFavorite
                                popularSearchAdapter.submitList(list)
                                popularSearchAdapter.notifyItemChanged(index)
                                return@registerForActivityResult
                            }
                        }
                    }
                }
            }
        }

    private fun watchDetail(placeId: String) {
        if (placeId.isEmpty()) return
        try {
            logE("Watch Detail", "Success")
            Intent(mActivity, DetailActivity::class.java).also { intent ->
                Bundle().also { b ->
                    b.putString(PLACE_ID, placeId)
                    intent.putExtras(b)
                    toDetail.launch(intent)
                }
            }
        } catch (e: Exception) {
            logE("Watch Detail", "Error")
            requireActivity().displayShortToast(getString(R.string.hint_error))
        }
    }

    private fun displayNotGpsDialog() {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, false, dialogBinding, false).let {
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

    private val getTextFromSpeechRecognizer =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val getText =
                        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
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
        if (!mActivity.requestAudioPermission())
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
        if (!mActivity.requestCameraPermission())
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
            .forResult(object : OnResultCallbackListener<LocalMedia> {
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
                distance = "NEAR"
                initSearchRv(dialogBinding)
                keyword = ""
                isHistory = true
                viewModel.getHistorySearchData()

                seekBarRange.max = (30 - 1) / 1 // (MAX - MIN) / STEP
                seekBarRange.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        val value = 1 + p1 * 1 // MIN + VALUE * STEP
                        distance = if (value < 10 && value == 1) "NEAR"
                        else if (value < 10) " $value"
                        else "$value"
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
                                    Coroutines.main {
                                        isHistory = false
                                        doSearch(radius)
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
                            Coroutines.main {
                                isHistory = false
                                doSearch(radius)
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
                                        doSearch(radius)
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

    private fun doSearch(distance: Long) {
        viewModel.autoComplete(
            location = if (viewModel.isUseMyLocation) Location(
                mActivity.myLatitude,
                mActivity.myLongitude
            ) else viewModel.selectLatLng,
            distance = distance,
            input = keyword
        )
    }

    private fun initSearchRv(dialogBinding: DialogSearchBinding) {
        searchAndHistoryAdapter = SearchAndHistoryAdapter()
        dialogBinding.rvResultAndHistory.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = searchAndHistoryAdapter

            searchAndHistoryAdapter.onItemClick = { historySearch ->
                viewModel.insertHistoryData(
                    AutoComplete(
                        place_id = historySearch.place_id,
                        name = historySearch.name,
                        address = historySearch.address,
                        description = historySearch.description,
                        isSearch = false
                    )
                )
                if (historySearch.place_id != "")
                    watchDetail(historySearch.place_id)
                else
                    Bundle().also { b ->
                        b.putString(LIST_TYPE, Constants.ListType.KEYWORD_LIST.name)
                        b.putString(KEYWORD, historySearch.name)
                        b.putInt(DISTANCE, (dialogBinding.seekBarRange.progress + 1) * 1000)
                        mActivity.start(ListActivity::class.java, b)
                    }
            }

            searchAndHistoryAdapter.onDeleteClick = { historySearch ->
                viewModel.deleteHistoryData(historySearch)
                viewModel.getHistorySearchData()
            }
        }
    }
}