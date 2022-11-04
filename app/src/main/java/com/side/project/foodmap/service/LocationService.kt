package com.side.project.foodmap.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationListenerCompat
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.util.Constants.PERMISSION_COARSE_LOCATION
import com.side.project.foodmap.util.Constants.PERMISSION_FINE_LOCATION
import com.side.project.foodmap.util.Method.logE

class LocationService(val context: Context) : Service(), LocationListenerCompat {
    // For GPS or Network Check
    private var checkGPS = false
    private var checkNetwork = false
    private var canGetLocation = false

    // For Location value
    private var loc: Location? = null
    private var latitude = 0.00
    private var longitude = 0.00

    // For Some Parameter
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10F
    private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong()

    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onLocationChanged(location: Location) {
    }

    private fun getLocation(): Location? {
        try {
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager?.let {
                // get GPS status
                checkGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                // get Network provider status
                checkNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!checkGPS && !checkNetwork)
                    context.displayShortToast(getString(R.string.hint_not_provider_gps))
                else {
                    this.canGetLocation = true

                    when {
                        checkGPS && checkNetwork -> { networkLocation() }
                        checkGPS && !checkNetwork -> { gpsLocation() }
                        !checkGPS && checkNetwork -> { networkLocation() }
                        !checkGPS && !checkNetwork -> { context.displayShortToast(getString(R.string.hint_not_provider_gps)) }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return loc
    }

    private fun gpsLocation() {
        if (checkGPS) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                context.displayShortToast(getString(R.string.hint_not_provider_gps))
            }
            logE("GPS-Location", "Turn on")
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this
            )
            loc = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            loc?.let {
                latitude = loc!!.latitude
                longitude = loc!!.longitude
            }
        }
    }

    private fun networkLocation() {
        if (checkNetwork) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                context.displayShortToast(getString(R.string.hint_not_provider_gps))
            }
            logE("Network-Location", "Turn on")
            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this
            )
            loc =
                locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            loc?.let {
                latitude = loc!!.latitude
                longitude = loc!!.longitude
            }
        }
    }

    @JvmName("getLongitude1")
    fun getLongitude(): Double {
        if (loc != null)
            longitude = loc!!.longitude
        return longitude
    }

    @JvmName("getLatitude1")
    fun getLatitude(): Double {
        if (loc != null)
            latitude = loc!!.latitude
        return latitude
    }

    fun canGetLocation(): Boolean = canGetLocation

    fun stopListener() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            logE("Location-Service", "Turn off")
            locationManager!!.removeUpdates(this@LocationService)
        }
    }
}