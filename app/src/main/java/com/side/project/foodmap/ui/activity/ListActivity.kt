package com.side.project.foodmap.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.databinding.ActivityListBinding
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.adapter.RestaurantListAdapter
import com.side.project.foodmap.ui.viewModel.ListViewModel
import com.side.project.foodmap.util.Constants.ListType
import com.side.project.foodmap.util.Constants.DISTANCE
import com.side.project.foodmap.util.Constants.IS_BLACK_LIST
import com.side.project.foodmap.util.Constants.IS_FAVORITE
import com.side.project.foodmap.util.Constants.KEYWORD
import com.side.project.foodmap.util.Constants.LIST_TYPE
import com.side.project.foodmap.util.Constants.PLACE_ID
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.launch
import java.util.*

class ListActivity : BaseActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: ListViewModel

    // Tool
    private lateinit var timer: Timer
    private lateinit var restaurantListAdapter: RestaurantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list)
        viewModel = ViewModelProvider(this)[ListViewModel::class.java]
        binding.paddingTop = getStatusBarHeight()

        initLocationService()
        getArguments()
        dialog.showLoadingDialog(mActivity, false)

        checkNetWork { onBackPressed() }


        binding.root.delayOnLifecycle(1000L) {
            viewModel.getUserRegionFromDataStore()
            doInitialize()
            initRvRestaurant()
            setDistanceListener()
            setListener()
        }
    }

    override fun onDestroy() {
        if (::timer.isInitialized)
            timer.cancel()
        super.onDestroy()
    }

    private fun getArguments() {
        viewModel.apply {
            intent.extras?.let {
                listType = it.getString(LIST_TYPE, "") ?: ""
                keyword = it.getString(KEYWORD, "") ?: ""
                settingDistance = it.getInt(DISTANCE, 1000)
            }
        }
    }

    private fun doInitialize() {
        viewModel.apply {
            if (listType == "") {
                displayShortToast(getString(R.string.hint_no_assign_type))
                finish()
            }

            // Set Title
            binding.title = when (listType) {
                ListType.NEAR_LIST.name -> {
                    binding.searchBar.gone()
                    binding.isHideDistance = false
                    getString(R.string.hint_near_region)
                }
                ListType.KEYWORD_LIST.name -> {
                    binding.searchBar.display()
                    binding.isHideDistance = false
                    binding.edSearch.setText(keyword)
                    binding.edSearch.text.toString().trim()
                }
                ListType.BLACK_LIST.name -> {
                    binding.searchBar.display()
                    binding.isHideDistance = true
                    getString(R.string.hint_black_list)
                }
                else -> getString(R.string.hint_near_region)
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    // 取得使用者區域
                    launch {
                        userRegion.collect { region ->
                            regionPlaceId = region
                            isUseMyLocation = region == ""
                            getSyncPlaceList()
                        }
                    }
                    // 取的區域設定列表
                    launch {
                        syncPlaceListFlow.collect { myPlaceLists ->
                            myPlaceLists.let { placeLists ->
                                placeLists.find { it.place_id == regionPlaceId }?.let {
                                    isUseMyLocation = false
                                    selectLatLng = it.location
                                }
                                callData(/* 第一次 */)
                            }
                        }
                    }
                    // 附近搜尋
                    launch {
                        distanceSearchFlow.collect { resource ->
                            when (resource) {
                                is Resource.Loading -> dialog.showLoadingDialog(mActivity, false)
                                is Resource.Success -> {
                                    dialog.cancelLoadingDialog()
                                    resource.data?.let { data ->
                                        binding.total = data.result.placeCount.toString()
                                        totalCount = data.result.placeCount
                                        repeatNum = totalCount / 50

                                        setObserveSearchData(data.result.placeList)
                                    }
                                }
                                is Resource.Error -> {
                                    dialog.cancelLoadingDialog()
                                    displayShortToast(getString(R.string.hint_error))
                                }
                                else -> Unit
                            }
                        }
                    }
                    // 關鍵字搜尋
                    launch {
                        keywordSearchFlow.collect { resource ->
                            when (resource) {
                                is Resource.Loading -> dialog.showLoadingDialog(mActivity, false)
                                is Resource.Success -> {
                                    dialog.cancelLoadingDialog()
                                    resource.data?.let { data ->
                                        binding.total = data.result.placeCount.toString()
                                        totalCount = data.result.placeCount
                                        repeatNum = totalCount / 50

                                        setObserveSearchData(data.result.placeList)
                                    }
                                }
                                is Resource.Error -> {
                                    dialog.cancelLoadingDialog()
                                    displayShortToast(getString(R.string.hint_error))
                                }
                                else -> Unit
                            }
                        }
                    }
                    // 黑名單
                    launch {
                        getSyncBlackListFlow.collect { placeList ->
                            placeList.let {
                                dialog.cancelLoadingDialog()
                                binding.total = placeList.size.toString()
                                binding.count = placeList.size.toString()
                                if (searchData == placeList)
                                    return@let
                                restaurantListAdapter.setPlaceList(placeList.toMutableList())
                                restaurantListAdapter.setIsBlackList(true)
                                restaurantListAdapter.setMyLocation(
                                    Location(myLatitude, myLongitude)
                                )
                            }
                        }
                    }
                    // 添加黑名單
                    launch {
                        pushBlackListFlow.collect {
                            when (it) {
                                is Resource.Success -> {
                                    mActivity.displayShortToast(getString(R.string.hint_success_push_black_list))
                                    val list = restaurantListAdapter.currentList.toMutableList()
                                    list.find { m -> m.place_id == blackPlaceId }?.let { placeList ->
                                        list.remove(placeList)
                                        restaurantListAdapter.setPlaceList(list)
                                        totalCount -= 1
                                        binding.total = totalCount.toString()
                                        binding.count = list.size.toString()
                                    }

                                }
                                is Resource.Error -> mActivity.displayShortToast(getString(R.string.hint_failed_push_black_list))
                                else -> Unit
                            }
                        }
                    }
                    // 觀察 List 資料變化
                    launch {
                        observeSearchData.observe(this@ListActivity) { resource ->
                            when (resource) {
                                is Resource.Success -> {
                                    resource.data?.let { placeList ->
                                        binding.count = placeList.size.toString()
                                        restaurantListAdapter.setPlaceList(placeList.toMutableList())
                                        restaurantListAdapter.setMyLocation(
                                            Location(myLatitude, myLongitude)
                                        )

                                        // Repeat Search
//                                        binding.edSearch.text.toString().trim().let {
//                                            if (it.isNotEmpty())
//                                                if (listType == ListType.KEYWORD_LIST.name)
//                                                    callData(true)
//                                                else
//                                                    filter(it)
//                                        }
                                    }
                                }
                                is Resource.Error -> {
                                    if (resource.message.equals("EMPTY")) {
                                        binding.count = "0"
                                        restaurantListAdapter.setPlaceList(emptyList())
                                        restaurantListAdapter.setMyLocation(
                                            Location(
                                                myLatitude,
                                                myLongitude
                                            )
                                        )
                                    } else
                                        displayShortToast(getString(R.string.hint_no_more_data))
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initRvRestaurant() {
        restaurantListAdapter = RestaurantListAdapter()
        binding.rvRestaurants.apply {
            layoutManager =
                GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)
            adapter = restaurantListAdapter
            setRvItemListener()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    viewModel.apply {
                        val firstItemPosition: Int =
                            (binding.rvRestaurants.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

                        if (firstItemPosition >= 1)
                            binding.fabUpTool.display()
                        else
                            binding.fabUpTool.gone()

                        if (!canScrollVertically(1)) {
                            // 判斷第幾次呼叫
                            if (alreadyCalledNum < repeatNum)
                                alreadyCalledNum++

                            if (viewModel.listType == ListType.BLACK_LIST.name)
                                return
                            callData(false, skip = (50 * alreadyCalledNum), limit = 50)
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // 不滾動時
                    if (newState == RecyclerView.SCROLL_STATE_IDLE)
                        binding.fabUpTool.delayOnLifecycle(1500L) {
                            binding.fabUpTool.gone()
                        }
                }
            })
        }
    }

    private val toDetail =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.extras?.let { b ->
                    val mPlaceId = b.getString(PLACE_ID, "")
                    val mIsFavorite = b.getBoolean(IS_FAVORITE, false)
                    val mIsBlackList = b.getBoolean(IS_BLACK_LIST, false)
                    val list = restaurantListAdapter.currentList.toMutableList()
                    list.forEachIndexed { index, placeList ->
                        if (placeList.place_id == mPlaceId) {
                            if (viewModel.listType == ListType.BLACK_LIST.name) {
                                if (!mIsBlackList) {
                                    list.remove(placeList)
                                    restaurantListAdapter.setPlaceList(list)
                                    restaurantListAdapter.notifyItemChanged(index)
                                    binding.total = list.size.toString()
                                    binding.count = list.size.toString()
                                }
                                return@registerForActivityResult
                            }
                            if (mIsBlackList) {
                                list.remove(placeList)
                                restaurantListAdapter.setPlaceList(list)
                                restaurantListAdapter.notifyItemChanged(index)
                                viewModel.totalCount -= 1
                                binding.total = viewModel.totalCount.toString()
                                binding.count = list.size.toString()
                                return@registerForActivityResult
                            }
                            if (mIsFavorite) {
                                placeList.isFavorite = mIsFavorite
                                restaurantListAdapter.setPlaceList(list)
                                restaurantListAdapter.notifyItemChanged(index)
                                return@registerForActivityResult
                            }
                        }
                    }
                }
            }
        }

    private fun setRvItemListener() {
        restaurantListAdapter.onItemClick = { placeId ->
            try {
                Method.logE("Watch Detail", "Success")
                Intent(mActivity, DetailActivity::class.java).also { intent ->
                    Bundle().also { b ->
                        b.putString(PLACE_ID, placeId)
                        intent.putExtras(b)
                        toDetail.launch(intent)
                    }
                }
            } catch (e: Exception) {
                Method.logE("Watch Detail", "Error")
                displayShortToast(getString(R.string.hint_error))
            }
        }

        restaurantListAdapter.onItemFavoriteClick = { placeId, isFavorite ->
            if (isFavorite) {
                viewModel.pullFavorite(arrayListOf(placeId))
                false
            } else {
                viewModel.pushFavorite(arrayListOf(placeId))
                true
            }
        }

        restaurantListAdapter.onItemBlackListClick = { placeId ->
            displayModifyBlackListDialog(placeId)
        }
    }

    private fun setDistanceListener() {
        viewModel.apply {
            binding.run {
                (settingDistance / 1000).let { value ->
                    seekBarRange.progress = value
                    distance = if (value < 10 && value == 1) "NEAR"
                    else if (value < 10) " $value"
                    else "$value"
                }
                seekBarRange.max = (30 - 1) / 1 // (MAX - MIN) / STEP
                seekBarRange.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        val value = 1 + p1 * 1 // MIN + VALUE * STEP
                        distance = if (value < 10 && value == 1) "NEAR"
                        else if (value < 10) " $value"
                        else "$value"
                        settingDistance = value * 1000
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        callData()
                    }
                })
            }
        }
    }

    private fun callData(
        isClear: Boolean = true,
        keyword: String = binding.edSearch.text.toString().trim(),
        skip: Int = 0,
        limit: Int = 50
    ) {
        viewModel.apply {
            if (isClear)
                searchData.clear()
            when (listType) {
                ListType.NEAR_LIST.name -> {
                    distanceSearch(
                        location = if (isUseMyLocation) Location(
                            mActivity.myLatitude,
                            mActivity.myLongitude
                        ) else selectLatLng,
                        distance = settingDistance,
                        skip = skip,
                        limit = limit
                    )
                }
                ListType.KEYWORD_LIST.name -> {
                    binding.title = binding.edSearch.text.toString().trim()
                    keywordSearch(
                        location = if (isUseMyLocation) Location(
                            mActivity.myLatitude,
                            mActivity.myLongitude
                        ) else selectLatLng,
                        keyword = keyword,
                        distance = settingDistance,
                        skip = skip,
                        limit = limit
                    )
                }
                ListType.BLACK_LIST.name -> getSyncBlackList()
                else -> Unit
            }
        }
    }

    private fun setListener() {
        binding.run {
            pullRefresh.setColorSchemeResources(R.color.primary)
            pullRefresh.setOnRefreshListener {
                if (viewModel.listType == ListType.BLACK_LIST.name) {
                    // 不做事
                    pullRefresh.isRefreshing = false
                    return@setOnRefreshListener
                }
                callData(/* 刷新資料 */)
                pullRefresh.isRefreshing = false
            }

            imgBack.setOnClickListener { onBackPressed() }

            fabUpTool.setOnClickListener {
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(this@ListActivity) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }
                smoothScroller.targetPosition = 0
                rvRestaurants.layoutManager?.startSmoothScroll(smoothScroller)
            }

            edSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (::timer.isInitialized)
                        timer.cancel()
                }

                override fun afterTextChanged(editable: Editable?) {
                    val text = editable.toString().trim()
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            Coroutines.main {
                                if (viewModel.listType == ListType.KEYWORD_LIST.name)
                                    callData()
                                else
                                    filter(text)
                            }
                        }
                    }, 500)
                }
            })
        }
    }

    private fun filter(text: String) {
        if (::restaurantListAdapter.isInitialized)
            restaurantListAdapter.filter.filter(text)
    }

    private fun displayModifyBlackListDialog(placeId: String) {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    imgPromptIcon.setImageResource(R.drawable.ic_report)
                    titleText = getString(R.string.hint_prompt_add_black_list_title)
                    tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                    tvConfirm.setOnClickListener {
                        viewModel.blackPlaceId = placeId
                        viewModel.pushBlackList(arrayListOf(placeId))
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }
}