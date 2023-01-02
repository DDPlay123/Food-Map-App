package com.side.project.foodmap.util.tools

import android.content.Context
import android.net.*
import androidx.lifecycle.LiveData

class NetworkConnection(context: Context) : LiveData<Boolean>() {

    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onActive() {
        super.onActive()
        updateConnection()
        connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback())
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback())
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    // For Android 5
//    private fun lollipopNetworkRequest() {
//        val requestBuilder = NetworkRequest.Builder()
//            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
//            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
//
//        connectivityManager.registerNetworkCallback(
//            requestBuilder.build(),
//            connectivityManagerCallback()
//        )
//    }

    private fun connectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                postValue(true)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                postValue(false)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }
        }
        return networkCallback
    }

    private fun updateConnection() {
        val networkCapabilities = connectivityManager.activeNetwork
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities)

        postValue(
            when {
                actNw?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> true
                actNw?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> true
                actNw?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> true
                else -> false
            }
        )
    }
}