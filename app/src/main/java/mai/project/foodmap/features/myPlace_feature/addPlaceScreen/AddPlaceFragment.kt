package mai.project.foodmap.features.myPlace_feature.addPlaceScreen

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mai.project.core.Configs
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.hideKeyboard
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.core.widget.recyclerView_decorations.DividerItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.checkGPSAndGetCurrentLocation
import mai.project.foodmap.base.checkLocationPermission
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateLoadingDialog
import mai.project.foodmap.databinding.FragmentAddPlaceBinding
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.SearchPlaceResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.utils.handleResult
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class AddPlaceFragment : BaseFragment<FragmentAddPlaceBinding, AddPlaceViewModel>(
    bindingInflater = FragmentAddPlaceBinding::inflate
), OnMapReadyCallback, OnMapLoadedCallback, OnCameraMoveListener, OnCameraIdleListener {
    override val viewModel by viewModels<AddPlaceViewModel>()

    private val args by navArgs<AddPlaceFragmentArgs>()

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var myMap: GoogleMap

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val pendingActions = mutableListOf<() -> Unit>()

    private val searchPlaceAdapter by lazy { SearchPlaceAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher = googleMapUtil.createLocationPermissionLauncher(
            fragment = this,
            onGranted = { },
            onDenied = { checkLocationPermission(googleMapUtil) }
        )
    }

    override fun FragmentAddPlaceBinding.initialize(savedInstanceState: Bundle?) {
        googleMapUtil.launchLocationPermission(locationPermissionLauncher)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapHost) as SupportMapFragment
        mapFragment.getMapAsync(this@AddPlaceFragment)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutSelector.root)
        with(bottomSheetBehavior) {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        with(layoutSelector.rvPlaceList) {
            addItemDecoration(
                DividerItemDecoration(
                    context = requireContext(),
                    direction = Direction.VERTICAL,
                    dividerHeight = 1.DP,
                    marginLeft = 16.DP,
                    marginRight = 16.DP,
                    dividerDrawableRes = R.drawable.bg_divider
                )
            )
            adapter = searchPlaceAdapter
        }
    }

    override fun FragmentAddPlaceBinding.destroy() {
        pendingActions.clear()
        if (::myMap.isInitialized) with(myMap) {
            clear()
            setOnMapLoadedCallback(null)
            setOnCameraMoveListener(null)
            setOnCameraIdleListener(null)
        }
    }

    override fun FragmentAddPlaceBinding.handleOnBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            popBackStack()
        }
    }

    override fun FragmentAddPlaceBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 是否顯示搜尋列表
            { isShowSearchList.collect(::handleIsShowSearchList) },
            // 關鍵資搜尋
            { searchFlow.debounce(300L).distinctUntilChanged().collect(::handleSearchFlow) },
            // 移動地圖後，搜尋地區資訊 (含經緯度)
            { searchPlaceResult.collect { handleSearchPlaceByLocationResult(it, false) } },
            // 輸入關鍵字後，回傳地區資訊列表 (不含經緯度)
            { searchPlacesResult.collect(::handleSearchPlacesResult) },
            // 點選關鍵字回傳的資訊列表後，取得地區資訊 (含經緯度)
            { placeDetailResult.collect { handleSearchPlaceByLocationResult(it, true) } },
            // 儲存定位點資訊
            { pushMyPlaceResult.collect(::handlePushMyPlaceResult) }
        )
    }

    override fun FragmentAddPlaceBinding.setListener() {
        imgBack.onClick { onBackPressed() }

        imgMyLocation.onClick {
            checkGPSAndGetCurrentLocation(
                googleMapUtil = googleMapUtil,
                onSuccess = { lat, lng -> initLocation(lat, lng) },
                onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
            )
        }

        layoutSelector.edSearch.doAfterTextChanged {
            viewModel.setSearchKeyword(it?.trim().toString())
        }

        searchPlaceAdapter.onItemClick = { item ->
            viewModel.getPlaceByAddress(item.address)
        }

        btnConfirm.onClick { viewModel.pushMyPlace() }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        viewModel.setShowSearchList(false)
                        pendingActions.forEach { it.invoke() }
                        pendingActions.clear()
                    }

                    BottomSheetBehavior.STATE_EXPANDED ->
                        viewModel.setShowSearchList(true)

                    BottomSheetBehavior.STATE_SETTLING, BottomSheetBehavior.STATE_DRAGGING ->
                        root.hideKeyboard

                    else -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val height = bottomSheet.height.toFloat()
                val offset = height * slideOffset

                viewModel.targetCameraPosition?.let {
                    when (bottomSheetBehavior.state) {
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                            setMapsPaddingFromBottom(offset)
                            googleMapUtil.updateCamera(myMap, it)
                        }

                        else -> Unit
                    }
                }
            }
        })
    }

    /**
     * 設定地圖與底部的偏移
     */
    private fun setMapsPaddingFromBottom(offset: Float) {
        val maxMapsPaddingBottom = 1f
        binding.layoutMap.setPadding(0, 0, 0, (offset * maxMapsPaddingBottom).roundToInt())
    }

    /**
     * 先檢查當前狀態是否為收合，如是收合，則直接執行。否則先暫存並設定 收合狀態
     */
    private fun checkCollapsedStateBeforeDoSomething(
        work: () -> Unit
    ) {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            pendingActions.add(work)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            work.invoke()
        }
    }

    override fun onMapReady(maps: GoogleMap) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main.immediate) {
            myMap = maps.apply {
                googleMapUtil.doInitializeGoogleMap(this, viewModel.themeMode.first())
                googleMapUtil.setCompassLocation(
                    mapFragment = mapFragment,
                    marginTop = 45.DP,
                    marginLeft = 90.DP
                )
                setOnMapLoadedCallback(this@AddPlaceFragment)
                setOnCameraMoveListener(this@AddPlaceFragment)
                setOnCameraIdleListener(this@AddPlaceFragment)
            }
        }
    }

    override fun onMapLoaded() {
        checkGPSAndGetCurrentLocation(
            googleMapUtil = googleMapUtil,
            onSuccess = { lat, lng -> initLocation(lat, lng) },
            onFailure = { initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE) }
        )
    }

    override fun onCameraMove() {
        if (viewModel.targetCameraPosition != myMap.cameraPosition)
            viewModel.targetCameraPosition = myMap.cameraPosition
    }

    override fun onCameraIdle() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
            viewModel.searchPlacesByLocation()
    }

    /**
     * 初始化位置
     */
    private fun initLocation(lat: Double, lng: Double) {
        checkCollapsedStateBeforeDoSomething {
            if (::myMap.isInitialized) {
                googleMapUtil.animateCamera(
                    map = myMap,
                    latLng = LatLng(lat, lng),
                    zoomLevel = 15f
                )
            }
        }
    }

    /**
     * 處理是否顯示搜尋列表
     */
    private fun handleIsShowSearchList(isShow: Boolean) = with(binding.layoutSelector) {
        tvCurrentAddress.isVisible = !isShow
        groupSearch.isVisible = isShow
        rvPlaceList.isVisible = isShow && searchPlaceAdapter.itemCount > 0
        lottieNoData.isVisible = isShow && searchPlaceAdapter.itemCount <= 0
    }

    /**
     * 處理關鍵字搜尋
     */
    private fun handleSearchFlow(keyword: String) {
        if (keyword.isNotEmpty()) {
            viewModel.searchPlacesByKeyword(keyword)
        } else {
            updateSearchPlaceList(emptyList())
        }
    }

    /**
     * 更新搜尋列表
     */
    private fun updateSearchPlaceList(
        list: List<SearchPlaceResult>?
    ) = with(binding.layoutSelector) {
        searchPlaceAdapter.submitList(list) {
            rvPlaceList.isVisible = viewModel.isShowSearchList.value && searchPlaceAdapter.itemCount > 0
            lottieNoData.isVisible = viewModel.isShowSearchList.value && searchPlaceAdapter.itemCount <= 0
        }
    }

    /**
     * 處理經緯度搜尋地區資訊狀態
     */
    private fun handleSearchPlaceByLocationResult(
        event: Event<NetworkResult<SearchPlaceResult>>,
        isFromAddress: Boolean
    ) = with(binding) {
        handleBasicResult(
            event = event,
            workOnSuccess = { data ->
                data?.let {
                    layoutSelector.tvCurrentAddress.text = String.format(Locale.getDefault(), "%s\n%s", it.name, it.address)
                    viewModel.selectedPlace = it
                    if (isFromAddress) checkCollapsedStateBeforeDoSomething {
                        googleMapUtil.animateCamera(
                            map = myMap,
                            latLng = LatLng(it.lat!!, it.lng!!)
                        )
                    }
                }
            }
        )
    }

    /**
     * 處理回傳的回傳地區資訊列表狀態
     */
    private fun handleSearchPlacesResult(
        event: Event<NetworkResult<List<SearchPlaceResult>>>
    ) {
        handleBasicResult(
            event = event,
            workOnSuccess = { updateSearchPlaceList(it) },
            workOnError = { updateSearchPlaceList(emptyList()) }
        )
    }

    /**
     * 處理儲存定位點狀態
     */
    private fun handlePushMyPlaceResult(
        event: Event<NetworkResult<EmptyNetworkResult>>
    ) {
        event.getContentIfNotHandled?.handleResult {
            onLoading = { navigateLoadingDialog(isOpen = true, cancelable = false) }
            onSuccess = {
                navigateLoadingDialog(false)
                setFragmentResult(
                    args.requestCode,
                    bundleOf(args.requestCode to "Whatever")
                )
                navigateUp()
            }
            onError = { _, msg ->
                navigateLoadingDialog(false)
                displayToast(msg ?: "Unknown Error")
            }
        }
    }
}