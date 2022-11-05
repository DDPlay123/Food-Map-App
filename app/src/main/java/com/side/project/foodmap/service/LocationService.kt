package com.side.project.foodmap.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationListenerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.util.Constants.PERMISSION_COARSE_LOCATION
import com.side.project.foodmap.util.Constants.PERMISSION_FINE_LOCATION
import com.side.project.foodmap.util.Method.logE

class LocationService : Service(), LocationListenerCompat {
    companion object {
        // For Some Parameter
        private const val minDistanceM: Float = 5F // 公尺
        private const val minTimeMs = (1000 * 5 * 1).toLong() // 毫秒
    }

    // For GPS or Network Check
    private var checkGPS = false
    private var checkNetwork = false
    private var canGetLocation = false

    // For Location value
    private var locationManager: LocationManager? = null
    private var mLocation: Location? = null

    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double>
        get() = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double>
        get() = _longitude

    init {
        getLocation()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onLocationChanged(location: Location) {
        logE("MyLocation", "Lat:${location.latitude}\tLong:${location.longitude}")
        _latitude.value = location.latitude
        _longitude.value = location.longitude
    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
    }

    private fun getLocation(): Location? {
        try {
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager?.let {
                // get GPS status
                checkGPS = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                // get Network provider status
                checkNetwork = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

                if (!checkGPS && !checkNetwork)
                    this.displayShortToast(getString(R.string.hint_not_provider_gps))
                else {
                    this.canGetLocation = true

                    when {
                        checkGPS && checkNetwork -> networkLocation()
                        checkGPS && !checkNetwork -> gpsLocation()
                        !checkGPS && checkNetwork -> networkLocation()
                        !checkGPS && !checkNetwork -> this.displayShortToast(getString(R.string.hint_not_provider_gps))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mLocation
    }

    @SuppressLint("MissingPermission")
    private fun gpsLocation() {
        if (checkGPS) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    PERMISSION_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                this.displayShortToast(getString(R.string.hint_not_provider_gps))
                return
            }
            logE("GPS-Location", "Turn on")
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTimeMs,
                minDistanceM, this
            )
            mLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            mLocation?.let {
                _latitude.postValue(mLocation?.latitude)
                _longitude.postValue(mLocation?.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun networkLocation() {
        if (checkNetwork) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    PERMISSION_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                this.displayShortToast(getString(R.string.hint_not_provider_gps))
                return
            }
            logE("Network-Location", "Turn on")
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTimeMs,
                minDistanceM, this
            )
            mLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            mLocation?.let {
                _latitude.postValue(mLocation?.latitude)
                _longitude.postValue(mLocation?.longitude)
            }
        }
    }

    fun canGetLocation(): Boolean = canGetLocation

    fun stopListener() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    PERMISSION_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            logE("Location-Service", "Turn off")
            locationManager?.removeUpdates(this@LocationService)
        }
    }
}