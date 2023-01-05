package com.side.project.foodmap.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ActivityMainBinding
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        doInitialize()
    }

    fun switchFragment(target: Int) {
        bottomNavigationView.selectedItemId = target
    }

    fun isHiddenNavigationBar(isHiddenBar: Boolean) {
        if (isHiddenBar)
            bottomNavigationView.gone()
        else
            bottomNavigationView.display()
    }

    private fun doInitialize() {
        bottomNavigationView = binding.bottomNavigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        viewModel.getUserImage()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 取得使用者照片
                launch {
                    viewModel.getUserImageFlow.collect {
                        when (it) {
                            is Resource.Success -> viewModel.getUserPictureFromDataStore()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}