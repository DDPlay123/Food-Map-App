package com.side.project.foodmap.ui.activity.launch

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ActivityLoginBinding
import com.side.project.foodmap.ui.activity.BaseActivity

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
}