package com.side.project.foodmap.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.model.LatLng
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
import com.side.project.foodmap.util.Constants.IS_NEAR_SEARCH
import com.side.project.foodmap.util.Constants.KEYWORD
import com.side.project.foodmap.util.Constants.LATITUDE
import com.side.project.foodmap.util.Constants.LONGITUDE
import com.side.project.foodmap.util.Constants.PLACE_ID
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.tools.Method.logE
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.Method.getDistance
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Timer
import java.util.TimerTask
import kotlin.Exception
import kotlin.collections.ArrayList
import kotlin.math.abs

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val viewModel: MainViewModel by activityViewModel()

    private lateinit var regionList: ArrayList<String>
    private lateinit var region: String
    private lateinit var placeId: String
    private var regionID: Int = 0
    private var keyword: String = ""

    private var isRecentPopularSearch: Boolean = true

    private lateinit var searchAndHistoryAdapter: SearchAndHistoryAdapter
    private lateinit var popularSearchAdapter: PopularSearchAdapter

    private lateinit var oldLatLng: Location

    init {
        Method.getFcmToken { token -> viewModel.putFcmToken(token) }
    }

    private val openGps = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
                        viewModel.nearSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude))
                        viewModel.popularSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude),
                            if (isRecentPopularSearch) 0 else 1)
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
                        if (getDistance(LatLng(location.lat, location.lng), LatLng(oldLatLng.lat,oldLatLng.lng)) * 1000 > 100) {
                            oldLatLng = location
                            if (::region.isInitialized) {
                                viewModel.nearSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude))
                                viewModel.popularSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude),
                                    if (isRecentPopularSearch) 0 else 1)
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
                                        searchAndHistoryAdapter.setSearchList(false, keyword, data)
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
                            searchAndHistoryAdapter.setSearchList(true, keyword, historySearchList)
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
                    mActivity.displayShortToast("Camera")
                }
            }

            imgSoundSearch.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    mActivity.displayShortToast("Sound")
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
                        viewModel.nearSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude))
                        viewModel.popularSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude),
                            if (isRecentPopularSearch) 0 else 1)
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
                    val latLng: LatLng = Method.getCurrentLatLng(region, LatLng(mActivity.myLatitude, mActivity.myLongitude))
                    b.putString(KEYWORD, region)
                    b.putBoolean(IS_NEAR_SEARCH, true)
                    b.putDouble(LATITUDE, latLng.latitude)
                    b.putDouble(LONGITUDE, latLng.longitude)
                    mActivity.start(ListActivity::class.java, b)
                }
            }
        }
    }

    private fun togglePopularSearch(isRecentPopularSearch: Boolean) {
        binding.isPopularSearch = isRecentPopularSearch
        if (isRecentPopularSearch)
            viewModel.popularSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude), mode = 0)
        else
            viewModel.popularSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude), mode = 1)
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
                    if (!mActivity.checkDeviceGPS() || !mActivity.checkNetworkGPS() && region == getString(R.string.hint_near_region))
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
                popularSearchAdapter.setMyLocation(LatLng(mActivity.myLatitude, mActivity.myLongitude))
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
                        popularSearchAdapter.getDataSize() - 1 ->  {
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

    private fun displaySearchDialog() {
        val dialogBinding = DialogSearchBinding.inflate(layoutInflater)
        var timer: Timer? = null
        dialog.showBottomDialog(mActivity, dialogBinding, true).let {
            dialogBinding.run {
                initSearchRv(dialogBinding)
                isHistory = true

                imgBack.setOnClickListener { dialog.cancelBottomDialog() }

                imgCameraSearchOrClear.setOnClickListener {
                    mActivity.displayShortToast("Camera")
                }

                imgSoundSearchOrClear.setOnClickListener {
                    mActivity.displayShortToast("Sound")
                }

                tvClear.setOnClickListener {
                    viewModel.deleteAllHistoryData()
                    searchAndHistoryAdapter.setSearchList(true, keyword, emptyList())
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
                                    Handler(Looper.getMainLooper()).post {
                                        isHistory = false
                                        viewModel.autoComplete(
                                            input = keyword,
                                            region = region,
                                            latLng = LatLng(mActivity.myLatitude, mActivity.myLongitude),
                                            radius = 10000
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
            viewModel.getHistorySearchData()
            searchAndHistoryAdapter.onItemClick = { historySearch ->
                viewModel.insertHistoryData(historySearch)
                if (historySearch.place_id != "")
                    watchDetail(historySearch.place_id)
            }
        }
    }
}