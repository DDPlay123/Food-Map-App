package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.Location
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.databinding.FragmentMapsBinding
import com.side.project.foodmap.helper.delayOnLifecycle
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.getLocation
import com.side.project.foodmap.ui.activity.DetailActivity
import com.side.project.foodmap.ui.adapter.MapRestaurantAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.abs

class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {
    private val viewModel: MainViewModel by activityViewModel()

    private lateinit var regionList: ArrayList<String>
    private lateinit var region: String
    private var regionID: Int = 0

    private lateinit var map: GoogleMap

    private lateinit var oldLatLng: Location

    private lateinit var mapRestaurantAdapter: MapRestaurantAdapter

    override fun FragmentMapsBinding.initialize() {
        mActivity.initLocationService()
        regionList = ArrayList(listOf(*resources.getStringArray(R.array.search_type)))
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()
    }

    private fun initGoogleMap() {
        if (!requestLocationPermission() || !::map.isInitialized)
            return
        map.apply {
            uiSettings.setAllGesturesEnabled(true)
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        doInitialize()
        initRestaurantVp()
        setListener()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 附近搜尋
                launch {
                    viewModel.nearSearchState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                Method.logE("Near Search", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                Method.logE("Near Search", "Success")
                                return@observe
                            }
                            is Resource.Error -> {
                                Method.logE("Near Search", "Error:${resource.message.toString()}")
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
                                Method.logE("Near Search Room", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                Method.logE("Near Search Room", "Success")
                                dialog.cancelLoadingDialog()
                                resource.data?.let { data ->
                                    setMapMarkers(data)
                                    if (::mapRestaurantAdapter.isInitialized)
                                        mapRestaurantAdapter.setData(data.result.placeList)
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                Method.logE("Near Search Room", "Error:${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 取得使用者區域設定
                launch {
                    viewModel.userRegion.collect { region ->
                        this@MapsFragment.region = region
                        regionID = regionList.indexOf(region)
                        setMyLocation(region)
                    }
                }
                // 自動更新
                launch {
                    mActivity.locationGet.observe(viewLifecycleOwner) { location ->
                        if (!::oldLatLng.isInitialized) {
                            oldLatLng = location
                            return@observe
                        }
                        if (Method.getDistance(LatLng(location.lat, location.lng), LatLng(oldLatLng.lat, oldLatLng.lng)) * 1000 > 100) {
                            oldLatLng = location
                            if (::region.isInitialized)
                                viewModel.nearSearch(region, LatLng(mActivity.myLatitude, mActivity.myLongitude))
                        }
                    }
                }
            }
        }
    }

    private fun setListener() {
        binding.run {

        }
    }

    private fun setMapMarkers(distanceSearchRes: DistanceSearchRes) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!::map.isInitialized) return@launch
            distanceSearchRes.result.placeList.forEach { index ->
                val markerOption = MarkerOptions().apply {
                    position(LatLng(index.location.lat, index.location.lng))
                    title(index.name)
                }
                map.addMarker(markerOption)
            }
        }
    }

    private fun setMyLocation(region: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (!::map.isInitialized) return@launch
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                if (region.getLocation().first == 0.0)
                    LatLng(mActivity.myLatitude, mActivity.myLongitude)
                else
                    LatLng(region.getLocation().first, region.getLocation().second),
                DEFAULT_ZOOM
            ))
        }
    }

    private fun setCenterLocation(latLng: LatLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(latLng.latitude, latLng.longitude),
            DEFAULT_ZOOM
        ))
    }

    private fun initRestaurantVp() {
        mapRestaurantAdapter = MapRestaurantAdapter()
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.apply {
            addTransformer(MarginPageTransformer(20))
        }
        binding.vpRestaurant.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(compositePageTransformer)
            adapter = mapRestaurantAdapter
            setVpItemListener()

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    delayOnLifecycle(500) {
                        setCenterLocation(
                            LatLng(
                                mapRestaurantAdapter.getData(position).location.lat,
                                mapRestaurantAdapter.getData(position).location.lng
                            )
                        )
                    }
                }
            })
        }
    }

    private fun setVpItemListener() {
        mapRestaurantAdapter.apply {
            onItemClick = { placeId ->
                watchDetail(placeId)
            }
        }
    }

    private fun watchDetail(placeId: String) {
        if (placeId.isEmpty()) return
        try {
            Method.logE("Watch Detail", "Success")
            Bundle().also { b ->
                b.putString(Constants.PLACE_ID, placeId)
                mActivity.start(DetailActivity::class.java, b)
            }
        } catch (e: Exception) {
            Method.logE("Watch Detail", "Error")
            requireActivity().displayShortToast(getString(R.string.hint_error))
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 18F
    }
}