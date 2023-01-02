package com.side.project.foodmap.ui.activity.launch

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ActivityLoginBinding
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.viewModel.LoginViewModel

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
    }
}