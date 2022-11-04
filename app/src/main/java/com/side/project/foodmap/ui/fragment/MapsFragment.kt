package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentMapsBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.service.LocationService
import com.side.project.foodmap.util.Constants.PERMISSION_COARSE_LOCATION
import com.side.project.foodmap.util.Constants.PERMISSION_FINE_LOCATION
import com.side.project.foodmap.util.Method.requestPermission

class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {
    private lateinit var locationService: LocationService
    private var map: GoogleMap? = null
    private var myLatitude: Double = 25.043871531367014
    private var myLongitude: Double = 121.53453374432904

    override fun FragmentMapsBinding.initialize() {
        val permission = arrayOf(PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION)
        if (!requestPermission(mActivity, *permission))
            findNavController().navigate(R.id.action_mapsFragment_to_homeFragment)

        initLocationService()
    }

    private fun initLocationService() {
        locationService = LocationService(requireContext())
        if (locationService.canGetLocation()) {
            myLatitude = locationService.getLatitude()
            myLongitude = locationService.getLongitude()
        } else
            mActivity.displayShortToast(getString(R.string.hint_not_provider_gps))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationService.stopListener()
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
       googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
           LatLng(myLatitude, myLongitude), DEFAULT_ZOOM
       ))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setListener() {
        binding.run {

        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15F
    }
}