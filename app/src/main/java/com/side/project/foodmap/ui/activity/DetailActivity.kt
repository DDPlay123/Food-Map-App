package com.side.project.foodmap.ui.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.ActivityDetailBinding
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.viewModel.DetailViewModel
import com.side.project.foodmap.util.Constants.PLACE_ID

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: DetailViewModel
    lateinit var placeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        intent.extras?.let { placeId = it.getString(PLACE_ID, "") ?: "" }
        checkNetWork { onBackPressed() }
    }
}