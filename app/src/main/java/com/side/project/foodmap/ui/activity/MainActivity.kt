package com.side.project.foodmap.ui.activity

import android.os.Bundle
import com.side.project.foodmap.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}