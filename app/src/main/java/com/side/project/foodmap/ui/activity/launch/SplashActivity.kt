package com.side.project.foodmap.ui.activity.launch

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.viewModel.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var splashScreen: SplashScreen
    private val viewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable support for Splash Screen API for
        // proper Android 12+ support
        splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }

        doInitialize()
        viewModel.getUserIsLoginFromDataStore()
    }

    private fun doInitialize() {
        viewModel.userIsLogin.observe(this) { isLogin ->
            if (isLogin)
                startActivity(Intent(this, MainActivity::class.java))
            else
                startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}