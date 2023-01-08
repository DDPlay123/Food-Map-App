package com.side.project.foodmap.ui.activity.other

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.MyPlaceList
import com.side.project.foodmap.databinding.ActivityGetLocationBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.adapter.RegionItemAdapter
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.GetLocationViewModel
import com.side.project.foodmap.util.Constants.REGION_PLACE_ID
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class GetLocationActivity : BaseActivity() {
    private lateinit var binding: ActivityGetLocationBinding
    private lateinit var viewModel: GetLocationViewModel
    private lateinit var map: GoogleMap
    private lateinit var mLoc: LatLng

    private var timer: Timer? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var regionItemAdapter: RegionItemAdapter

    private lateinit var oldLatLng: Location
    private val _moveLatLng = MutableLiveData<Location>()
    private val moveLatLng: LiveData<Location>
        get() = _moveLatLng

    // wait push data
    private var mPlaceId = ""
    private var mName = ""
    private var mAddress = ""
    private var mLocation = Location(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_get_location)
        viewModel = ViewModelProvider(this)[GetLocationViewModel::class.java]
        binding.paddingTop = getStatusBarHeight()
        binding.layoutOption.isLocation = true

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        checkNetWork { onBackPressed() }

        initLocationService()
        doInitialize()
        initLayoutOption()
        initRvAutocomplete()
        setListener()
    }

    override fun onDestroy() {
        map.clear()
        timer?.cancel()
        timer = null
        super.onDestroy()
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()

        map.setOnCameraIdleListener {
            map.cameraPosition.target.let {
                _moveLatLng.postValue(Location(it.latitude, it.longitude))
            }
        }

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
            uiSettings.isCompassEnabled = false
        }
    }

    private fun setMyLocation() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!::map.isInitialized) return@launch
            viewModel.autocompleteByLocation(Location(myLatitude, myLongitude))
            mLoc = LatLng(mActivity.myLatitude, mActivity.myLongitude)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(mActivity.myLatitude, mActivity.myLongitude),
                    DEFAULT_ZOOM
                )
            )
        }
    }

    private fun setLocation(location: Location) {
        if (!::map.isInitialized) return
        map.animateCamera(
            CameraUpdateFactory.newLatLng(LatLng(location.lat, location.lng))
        )
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 位置監聽
                launch {
                    moveLatLng.observe(this@GetLocationActivity) { latLng ->
                        if (!::oldLatLng.isInitialized) {
                            oldLatLng = latLng
                            return@observe
                        }
                        viewModel.autocompleteByLocation(latLng)
                    }
                }
                // 自動填充(Location)
                launch {
                    viewModel.locationAutoCompleteFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let {
                                    binding.layoutOption.run {
                                        tvName.text = it.result.placeList[0].name
                                        tvAddress.text = it.result.placeList[0].address
                                    }
                                    // fill data
                                    it.result.placeList[0].apply {
                                        mPlaceId = place_id
                                        mName = name
                                        mAddress = address
                                        mLocation = location
                                    }
                                }
                            }
                            is Resource.Error -> displayShortToast(getString(R.string.hint_not_found_location))
                            else -> Unit
                        }
                    }
                }
                // 自動填充(關鍵字)
                launch {
                    viewModel.keywordAutoCompleteFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let {
                                    if (!::regionItemAdapter.isInitialized) return@collect
                                    if (it.result.placeList.size <= 0) toggleRvAnim(true)
                                    else toggleRvAnim(false)
                                    regionItemAdapter.submitList(it.result.placeList.toMutableList())
                                }
                            }
                            is Resource.Error -> displayShortToast(getString(R.string.hint_not_found_location))
                            else -> Unit
                        }
                    }
                }
                // 取的經緯度
                launch {
                    viewModel.getLocationFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let {
                                    it.result.place.location.let { l ->
                                        delay(200)
                                        setLocation(l)
                                        mLocation = l
                                    }
                                }
                            }
                            is Resource.Error -> displayShortToast(getString(R.string.hint_not_found_location))
                            else -> Unit
                        }
                    }
                }
                // 上傳座標
                launch {
                    viewModel.pushPlaceListFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                displayShortToast(getString(R.string.text_success))
                                Intent().apply {
                                    putExtra(REGION_PLACE_ID, mPlaceId)
                                    setResult(RESULT_OK, this)
                                    finish()
                                }
                            }
                            is Resource.Error -> displayShortToast(getString(R.string.hint_not_found_location))
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun initLayoutOption() {
        binding.apply {
            bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutOption.layoutSelector)
            with (bottomSheetBehavior) {
                skipCollapsed = true

                state = BottomSheetBehavior.STATE_COLLAPSED
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                toggleLayout(true)
                                hideKeyboard()
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                toggleLayout(false)
//                                binding.layoutOption.edSearch.showKeyboard()
                            }
                            BottomSheetBehavior.STATE_SETTLING -> binding.layoutOption.edSearch.hideKeyboard()
                            BottomSheetBehavior.STATE_DRAGGING -> binding.layoutOption.edSearch.hideKeyboard()
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

    private fun toggleLayout(isShowLocation: Boolean) {
        binding.layoutOption.run { isLocation = isShowLocation }
    }

    private fun setMapPaddingBottom(offset: Float) {
        //From 0.0 (min) - 1.0 (max) // bsExpanded - bsCollapsed;
        val maxMapPaddingBottom = 1.0f
        binding.layoutMap.setPadding(0, 0, 0, (offset * maxMapPaddingBottom).roundToInt())
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding.run {
            imgBack.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    mActivity.onBackPressed()
                }
            }

            imgMyLocation.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    setMyLocation()
                }
            }

            layoutOption.edSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    timer?.cancel()
                }

                override fun afterTextChanged(editable: Editable?) {
                    if (editable.toString().trim().isNotEmpty()) {
                        timer = Timer()
                        timer?.schedule(object : TimerTask() {
                            override fun run() {
                                viewModel.autocompleteByKeyword(
                                    Location(myLatitude, myLongitude),
                                    editable.toString().trim()
                                )
                            }
                        }, 500)
                    } else {
                        regionItemAdapter.submitList(emptyList())
                        toggleRvAnim(true)
                    }
                }
            })

            btnConfirm.setOnClickListener {
                viewModel.apply {
                    insertPlaceListData(MyPlaceList(mPlaceId, mName, mAddress, mLocation))
                    pushPlaceList(mPlaceId, mName, mAddress, mLocation)
                }
            }
        }
    }

    private fun initRvAutocomplete() {
        regionItemAdapter = RegionItemAdapter()
        binding.layoutOption.rvAutocomplete.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = regionItemAdapter

            regionItemAdapter.onItemClick = { item ->
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                item.apply {
                    mPlaceId = place_id
                    mName = name
                    mAddress = address
                }
                viewModel.getLocationByAddress(item.description)
            }
        }
    }

    private fun toggleRvAnim(isAnim: Boolean) {
        binding.layoutOption.let {
            if (isAnim) {
                it.rvAutocomplete.gone()
                it.lottieNoData.display()
            } else {
                it.rvAutocomplete.display()
                it.lottieNoData.gone()
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 18F
    }
}