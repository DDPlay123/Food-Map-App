package com.side.project.foodmap.ui.fragment

import android.annotation.SuppressLint
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
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.restaurant.DistanceSearchRes
import com.side.project.foodmap.databinding.FragmentMapsBinding
import com.side.project.foodmap.helper.delayOnLifecycle
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.getLocation
import com.side.project.foodmap.helper.requestLocationPermission
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

class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {
    private val viewModel: MainViewModel by activityViewModel()
    private lateinit var map: GoogleMap

    private lateinit var mapRestaurantAdapter: MapRestaurantAdapter

    private lateinit var oldLatLng: Location

    override fun FragmentMapsBinding.initialize() {
        mActivity.initLocationService()
        viewModel.distanceSearch(
            if (viewModel.isUseMyLocation) Location(
                mActivity.myLatitude,
                mActivity.myLongitude
            ) else viewModel.selectLatLng
        )
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()
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
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog.showLoadingDialog(mActivity, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        doInitialize()
        initRestaurantVp()
        setListener()
    }

    override fun onDestroyView() {
        map.clear()
        super.onDestroyView()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 附近搜尋
                launch {
                    viewModel.distanceSearchFlow.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    setMapMarkers(data)
                                    if (::mapRestaurantAdapter.isInitialized)
                                        mapRestaurantAdapter.submitList(data.result.placeList.toMutableList())
                                }
                            }
                            is Resource.Error -> {
                                requireActivity().displayShortToast(getString(R.string.hint_error))
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
                        if (Method.getDistance(location, oldLatLng) * 1000 > 100) {
                            oldLatLng = location
                            viewModel.run {
                                distanceSearch(
                                    if (isUseMyLocation) Location(
                                        mActivity.myLatitude,
                                        mActivity.myLongitude
                                    ) else selectLatLng
                                )
                            }
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

    private fun setMyLocation() {
        viewModel.apply {
            lifecycleScope.launch(Dispatchers.Main) {
                if (!::map.isInitialized) return@launch
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    if (isUseMyLocation) LatLng(mActivity.myLatitude, mActivity.myLongitude)
                    else LatLng(selectLatLng.lat, selectLatLng.lng),
                    DEFAULT_ZOOM
                ))
            }
        }
    }

    private fun setCenterLocation(location: Location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(location.lat, location.lng),
            DEFAULT_ZOOM
        ))
    }

    private fun initRestaurantVp() {
        mapRestaurantAdapter = MapRestaurantAdapter()
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.apply {
            addTransformer(MarginPageTransformer(20))
        }
        binding?.vpRestaurant?.apply {
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
                    // TODO(顯示路線)
                }
            })
        }
    }

    private fun setVpItemListener() {
        mapRestaurantAdapter.apply {
            onItemClick = { placeList ->
                setCenterLocation(placeList.location)
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