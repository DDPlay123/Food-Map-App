package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentMapsBinding
import com.side.project.foodmap.util.Method.requestPermission

class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {
    private var map: GoogleMap? = null

    override fun FragmentMapsBinding.initialize() {
        if (!requestPermission(mActivity, *permission))
            findNavController().navigate(R.id.action_mapsFragment_to_homeFragment)
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(myLatitude, myLongitude), DEFAULT_ZOOM
            )
        )
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