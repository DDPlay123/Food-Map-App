package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentFavoritesBinding
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>(R.layout.fragment_favorites) {
    private val viewModel: MainViewModel by activityViewModel()

    override fun FragmentFavoritesBinding.initialize() {
        initLocationService()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {

    }

    private fun setListener() {

    }
}