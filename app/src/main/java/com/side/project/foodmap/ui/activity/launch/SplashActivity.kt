package com.side.project.foodmap.ui.activity.launch

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.side.project.foodmap.helper.checkDeviceGPS
import com.side.project.foodmap.helper.checkNetworkGPS
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.viewModel.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var splashScreen: SplashScreen
    private val viewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable support for Splash Screen API for
        // proper Android 12+ support
        splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        super.onCreate(savedInstanceState)

        doInitialize()
        viewModel.getUserIsLoginFromDataStore()
    }

    private fun doInitialize() {
        viewModel.userIsLogin.observe(this) { isLogin ->
            if (!isLogin || (!checkDeviceGPS() && !checkNetworkGPS()))
                startActivity(Intent(this, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            else
                startActivity(Intent(this, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}