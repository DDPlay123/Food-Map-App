package com.side.project.foodmap.ui.adapter.other

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.side.project.foodmap.ui.fragment.other.ImageFragment

class AlbumAdapter(fragment: Fragment, private val photosId: List<String>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = photosId.size

    override fun createFragment(position: Int): Fragment {
        return ImageFragment.newInstance(photosId[position])
    }
}