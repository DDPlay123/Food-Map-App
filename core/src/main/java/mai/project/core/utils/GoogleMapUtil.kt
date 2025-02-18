package mai.project.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Dimension
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapColorScheme
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Google Map 相關工具
 */
@Singleton
class GoogleMapUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationManager: LocationManager
) {
    /**
     * 定位權限
     */
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * 檢查定位權限是否開啟
     */
    val checkLocationPermission: Boolean
        get() = locationPermissions.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * 檢查 GPS 是否開啟
     */
    val checkGPS: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    /**
     * 建立請求定位權限的 Launcher
     */
    fun createLocationPermissionLauncher(
        fragment: Fragment,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.any { it.value }) onGranted() else onDenied()
        }
    }

    /**
     * 請求權限
     */
    fun launchLocationPermission(launcher: ActivityResultLauncher<Array<String>>) {
        launcher.launch(locationPermissions)
    }

    /**
     * 取得當前位置
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (lat: Double, lng: Double) -> Unit,
        onFailure: () -> Unit
    ) {
        if (checkLocationPermission && checkGPS) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Timber.d(message = "取得經緯度成功")
                    onSuccess(it.latitude, it.longitude)
                } ?: run {
                    Timber.e(message = "取得經緯度失敗，請求新的位置")
                    // 如果 lastLocation 為 null，請求新的位置更新
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.locations.lastOrNull()?.let { latestLocation ->
                                onSuccess(latestLocation.latitude, latestLocation.longitude)
                                // 取得一次即移除更新
                                fusedLocationClient.removeLocationUpdates(this)
                            } ?: onFailure()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                }
            }.addOnFailureListener {
                Timber.e(message = "取得經緯度失敗", t = it)
                onFailure()
            }
        } else {
            onFailure()
        }
    }

    /**
     * 初始化 GoogleMap
     *
     * @param map [GoogleMap] 地圖
     */
    @SuppressLint("MissingPermission")
    fun doInitializeGoogleMap(
        map: GoogleMap,
        themeMode: Int
    ) = with(map) {
        if (checkLocationPermission) isMyLocationEnabled = true
        uiSettings.setAllGesturesEnabled(true)
        uiSettings.isMyLocationButtonEnabled = false
        uiSettings.isMapToolbarEnabled = false
        uiSettings.isCompassEnabled = true

        mapColorScheme = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> MapColorScheme.DARK

            AppCompatDelegate.MODE_NIGHT_NO -> MapColorScheme.LIGHT

            else -> MapColorScheme.FOLLOW_SYSTEM
        }
    }

    /**
     * 移動地圖位置
     *
     * @param map [GoogleMap] 地圖
     * @param latLng [LatLng] 位置
     * @param zoomLevel [Float] 縮放等級
     */
    fun moveCamera(
        map: GoogleMap,
        latLng: LatLng,
        zoomLevel: Float? = null
    ) = with(map) {
        val update = zoomLevel?.let { CameraUpdateFactory.newLatLngZoom(latLng, it) }
            ?: CameraUpdateFactory.newLatLng(latLng)
        moveCamera(update)
    }

    /**
     * 更新地圖位置
     *
     * @param map [GoogleMap] 地圖
     * @param cameraPosition [CameraPosition] 位置
     */
    fun updateCamera(
        map: GoogleMap,
        cameraPosition: CameraPosition
    ) = with(map) {
        moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    /**
     * 移動地圖位置，並且帶有動畫
     *
     * @param map [GoogleMap] 地圖
     * @param latLng [LatLng] 位置
     * @param zoomLevel [Float] 縮放等級
     * @param finish [Function] 結束時的回調
     * @param cancel [Function] 取消時的回調
     */
    fun animateCamera(
        map: GoogleMap,
        latLng: LatLng,
        zoomLevel: Float? = null,
        finish: () -> Unit = {},
        cancel: () -> Unit = {}
    ) = with(map) {
        val update = zoomLevel?.let { CameraUpdateFactory.newLatLngZoom(latLng, it) }
            ?: CameraUpdateFactory.newLatLng(latLng)

        animateCamera(
            update,
            object : GoogleMap.CancelableCallback {
                override fun onFinish() = finish.invoke()
                override fun onCancel() = cancel.invoke()
            }
        )
    }

    /**
     * 設定地圖的指南針位置
     *
     * @param mapFragment [SupportMapFragment] 地圖片段
     * @param marginTop [Dimension] 上邊距
     * @param marginLeft [Dimension] 左邊距
     * @param marginRight [Dimension] 右邊距
     * @param marginBottom [Dimension] 下邊距
     */
    fun setCompassLocation(
        mapFragment: SupportMapFragment,
        @Dimension(unit = Dimension.DP)
        marginTop: Int = 0,
        @Dimension(unit = Dimension.DP)
        marginLeft: Int = 0,
        @Dimension(unit = Dimension.DP)
        marginRight: Int = 0,
        @Dimension(unit = Dimension.DP)
        marginBottom: Int = 0
    ) {
        try {
            mapFragment.view?.findViewWithTag<View>("GoogleMapMyLocationButton")?.parent?.let { parent ->
                val compassView = (parent as ViewGroup).getChildAt(4)
                val layoutParams =
                    RelativeLayout.LayoutParams(compassView.width, compassView.height).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                        addRule(RelativeLayout.ALIGN_PARENT_TOP)
                        setMargins(marginLeft, marginTop, marginRight, marginBottom)
                    }
                compassView.layoutParams = layoutParams
            }
        } catch (e: Exception) {
            Timber.e(message = "setCompass", t = e)
        }
    }

    /**
     * 解碼 Google Map API 路線
     */
    fun decodePolyline(polyline: String): List<LatLng> {
        var index = 0
        var lat = 0
        var lng = 0
        val coordinates = mutableListOf<LatLng>()

        while (index < polyline.length) {
            // 解碼緯度差值
            var shift = 0
            var result = 0
            var b: Int
            do {
                b = polyline[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)
            val deltaLat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lat += deltaLat

            // 解碼經度差值
            shift = 0
            result = 0
            do {
                b = polyline[index++].code - 63
                result = result or ((b and 0x1F) shl shift)
                shift += 5
            } while (b >= 0x20)
            val deltaLng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
            lng += deltaLng

            // 轉換為實際經緯度，並四捨五入到 5 位小數
            val latitude = round(lat / 1e5)
            val longitude = round(lng / 1e5)
            // 如需過濾 (0, 0) 座標，可保留以下判斷：
            if (latitude == 0.0 && longitude == 0.0) continue

            coordinates.add(LatLng(latitude, longitude))
        }

        return coordinates
    }

    // 四捨五入方法
    private fun round(value: Double, precision: Int = 5): Double {
        val factor = 10.0.pow(precision.toDouble())
        return (value * factor).toInt().toDouble() / factor
    }

    /**
     * 新增資訊框在路徑上
     *
     * @param map [GoogleMap] 地圖
     * @param routes 路徑
     * @param title 標題
     * @param content 內文
     */
    fun addInfoWindowOnPolyline(
        map: GoogleMap,
        routes: List<LatLng>,
        title: String,
        content: String
    ): Marker? {
        val pointsOnLine = routes.size
        val infoLatLng = routes[(pointsOnLine / 2)]
        val marker = map.addMarker(
            MarkerOptions()
                .position(infoLatLng)
                .title(title)
                .snippet(content)
                .alpha(0f)
                .icon(null)
                .anchor(0f, 0f)
        )
        marker?.showInfoWindow()
        return marker
    }
}