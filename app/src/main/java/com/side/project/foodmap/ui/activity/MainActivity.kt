package com.side.project.foodmap.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ActivityMainBinding
import com.side.project.foodmap.helper.getStatusBarHeight
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.viewModel.MainViewModel

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        doInitialize()
    }

    fun switchFragment(target: Int) {
        bottomNavigationView.selectedItemId = target
    }

    private fun doInitialize() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        bottomNavigationView = binding.bottomNavigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        binding.apply {
            paddingTop = getStatusBarHeight()
            paddingBottom = getNavigationBarHeight()
        }
    }

    private fun getNavigationBarHeight(): Int {
        var result = 0
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0)
            result = resources.getDimensionPixelSize(resourceId)

        return result
    }
}