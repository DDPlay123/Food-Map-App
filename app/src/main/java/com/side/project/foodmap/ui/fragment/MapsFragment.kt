package com.side.project.foodmap.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.databinding.FragmentMapsBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {
    private val viewModel: MainViewModel by activityViewModel()

    private var map: GoogleMap? = null
    private lateinit var distanceSearchRes: DistanceSearchRes

    override fun FragmentMapsBinding.initialize() {
        initLocationService()
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        initGoogleMap()
    }

    @SuppressLint("MissingPermission")
    private fun initGoogleMap() {
        map?.apply {
            uiSettings.setAllGesturesEnabled(true)
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(myLatitude, myLongitude), DEFAULT_ZOOM
            ))

            if (::distanceSearchRes.isInitialized)
                setMapMarkers()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 附近搜尋 From Room
                launch {
                    viewModel.getDistanceSearch.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                Method.logE("Near Search Room", "Loading")
                                dialog.showLoadingDialog(false)
                                return@observe
                            }
                            is Resource.Success -> {
                                Method.logE("Near Search Room", "Success")
                                dialog.cancelLoadingDialog()
                                resource.data?.let { data -> distanceSearchRes = data }
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
            }
        }
    }

    private fun setMapMarkers() {
        distanceSearchRes.result.placeList.forEach { index ->
            val markerOption = MarkerOptions().apply {
                position(LatLng(index.location.lat, index.location.lng))
                title(index.name)
            }
            map?.addMarker(markerOption)
        }
    }

    private fun setListener() {
        binding.run {

        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 18F
    }
}