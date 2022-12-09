package com.side.project.foodmap.util.tools

import android.content.Context
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import com.side.project.foodmap.data.remote.api.Location
import com.side.project.foodmap.helper.checkDeviceGPS
import com.side.project.foodmap.helper.checkNetworkGPS
import com.side.project.foodmap.util.Constants

class LocationGet(var context: Context) : LiveData<Location>() {
    companion object {
        private const val ONE_MINUTE: Long = 1000
        val locationRequest: LocationRequest =
            LocationRequest.create().apply {
                interval = ONE_MINUTE
                fastestInterval = ONE_MINUTE / 4
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun onActive() {
        super.onActive()
        if (!Method.hasPermissions(context, *Constants.permission) && (!context.checkDeviceGPS() || !context.checkNetworkGPS()))
            return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                setLocationData(it)
            }
        }
    }

    internal fun startLocationUpdates() {
        if (!Method.hasPermissions(context, *Constants.permission) && (!context.checkDeviceGPS() || !context.checkNetworkGPS()))
            return
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun setLocationData(location: android.location.Location) {
        Method.logE("MyLocation", "${location.latitude}, ${location.longitude}")
        location.let {
            value = Location(
                lat = it.latitude,
                lng = it.longitude
            )
        }
    }


    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            for (location in locationResult.locations)
                setLocationData(location = location)
        }
    }
}