package com.side.project.foodmap.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
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
import com.side.project.foodmap.util.tools.Method.logE

/**
 * 不好用，暫時廢棄。
 */
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

    // For Force Get Location value
    private var getLatitude = 0.00
    private var getLongitude = 0.00

    // For Observer Location value
    private var locationManager: LocationManager? = null
    private var mLocation: Location? = null

    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double>
        get() = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double>
        get() = _longitude

    fun startListener(context: Context) {
        getLocation(context)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onLocationChanged(location: Location) {
        logE("MyLocation", "Lat:${location.latitude}\tLong:${location.longitude}")
        _latitude.value = location.latitude
        _longitude.value = location.longitude
    }

    private fun getLocation(mContext: Context): Location? {
        try {
            locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager?.let {
                // get GPS status
                checkGPS = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                // get Network provider status
                checkNetwork = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

                if (!checkGPS && !checkNetwork)
                    mContext.displayShortToast(getString(R.string.hint_not_provider_gps))
                else {
                    this.canGetLocation = true

                    when {
                        checkGPS && checkNetwork -> networkLocation(mContext)
                        checkGPS && !checkNetwork -> gpsLocation(mContext)
                        !checkGPS && checkNetwork -> networkLocation(mContext)
                        !checkGPS && !checkNetwork -> mContext.displayShortToast(getString(R.string.hint_not_provider_gps))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mLocation
    }

    @SuppressLint("MissingPermission")
    private fun gpsLocation(context: Context) {
        if (checkGPS) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
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
                _latitude.value = mLocation?.latitude
                _longitude.value = mLocation?.longitude
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun networkLocation(context: Context) {
        if (checkNetwork) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    PERMISSION_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
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
                _latitude.value = mLocation?.latitude
                _longitude.value = mLocation?.longitude
            }
        }
    }

    fun canGetLocation(): Boolean = canGetLocation

    @JvmName("getLatitude")
    fun getLatitude(): Double {
        if (mLocation != null)
            getLatitude = mLocation!!.latitude
        return getLatitude
    }

    @JvmName("getLongitude")
    fun getLongitude(): Double {
        if (mLocation != null)
            getLongitude = mLocation!!.longitude
        return getLongitude
    }

    fun stopListener(context: Context) {
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
            locationManager?.removeUpdates(this@LocationService)
        }
    }
}