package com.side.project.foodmap.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ActivityMainBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.util.Constants.PERMISSION_COARSE_LOCATION
import com.side.project.foodmap.util.Constants.PERMISSION_CODE
import com.side.project.foodmap.util.Constants.PERMISSION_FINE_LOCATION

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        doInitialize()
    }

    private fun doInitialize() {
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}