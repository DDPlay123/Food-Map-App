package com.side.project.foodmap.ui.activity.other

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.helper.checkDeviceGPS
import com.side.project.foodmap.helper.checkNetworkGPS
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.DialogManager
import com.side.project.foodmap.util.tools.NetworkConnection
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Constants.PERMISSION_CODE
import com.side.project.foodmap.util.tools.LocationGet
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {
    companion object {
        const val DEFAULT_LATITUDE = 25.043871531367014
        const val DEFAULT_LONGITUDE = 121.53453374432904
    }

    val networkConnection: NetworkConnection by inject()

    lateinit var mActivity: BaseActivity
    val dialog: DialogManager by inject()
    val animManager: AnimManager by inject()

    val locationGet: LocationGet by inject()
    var myLatitude: Double = DEFAULT_LATITUDE
    var myLongitude: Double = DEFAULT_LONGITUDE

    init {
        // 清空ViewModel，避免記憶體洩漏。
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_STOP) {
                    window?.let {
                        if (window.peekDecorView() != null)
                            window.peekDecorView().cancelPendingInputEvents()
                    }
                }
            }
        })

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    if (!isChangingConfigurations)
                        viewModelStore.clear()
                }
            }
        })
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level <= TRIM_MEMORY_BACKGROUND)
            System.gc()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this

        checkNetWork { /** 執行要做的方法 **/ }
    }

    fun checkNetWork(work: (() -> Unit)) {
        networkConnection.observe(this) { isConnect ->
            if (!isConnect) {
                val binding = DialogPromptBinding.inflate(layoutInflater)
                dialog.cancelAllDialog()
                dialog.showCenterDialog(mActivity, false, binding, false).let {
                    binding.run {
                        showIcon = true
                        hideCancel = true
                        imgPromptIcon.setImageResource(R.drawable.ic_wifi_off)
                        titleText = getString(R.string.hint_internet_error_title)
                        subTitleText = getString(R.string.hint_internet_error_subtitle)
                        tvConfirm.setOnClickListener {
                            dialog.cancelCenterDialog()
                            work()
                        }
                    }
                }
            }
        }
    }

    private val openGps =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result?.let {
            try {
                mActivity.initLocationService()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkMyDeviceGPS(): Boolean {
        if (!checkDeviceGPS() && !checkNetworkGPS()) {
            displayShortToast(getString(R.string.hint_prompt_not_gps_title))
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                openGps.launch(this)
                return false
            }
        }
        return true
    }

    fun initLocationService() {
        if (!checkDeviceGPS() && !checkNetworkGPS()) {
            displayShortToast(getString(R.string.hint_not_provider_gps))
            return
        }
        locationGet.startLocationUpdates()
        locationGet.observe(this) { location ->
            myLatitude = location.lat
            myLongitude = location.lng
        }
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String =
        Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                for (result in grantResults)
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        when {
                            permissions.any { it == Constants.PERMISSION_FINE_LOCATION || it == Constants.PERMISSION_COARSE_LOCATION } ->
                                displayShortToast(getString(R.string.hint_not_location_permission))
                        }
                    }
            }
        }
    }

    fun start(next: Class<*>, bundle: Bundle?, finished: Boolean) {
        Intent(applicationContext, next).also { intent ->
            if (bundle == null)
                intent.putExtras(Bundle())
            else
                intent.putExtras(bundle)
            // jump activity
            startActivity(intent)
            // close activity
            if (finished)
                this.finish()
        }
    }

    fun start(next: Class<*>) {
        this.start(next, null, false)
    }

    fun start(next: Class<*>, bundle: Bundle?) {
        this.start(next, bundle, false)
    }

    fun start(next: Class<*>, finished: Boolean) {
        this.start(next, null, finished)
    }
}