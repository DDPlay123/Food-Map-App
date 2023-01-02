package com.side.project.foodmap.ui.fragment.launch

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentIntroductionBinding
import com.side.project.foodmap.helper.setAnimClick
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.other.AnimState

class IntroductionFragment : BaseFragment<FragmentIntroductionBinding>(R.layout.fragment_introduction) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
    }

    private fun setListener() {
        binding?.btnNext?.setOnClickListener {
            val anim = animManager.smallToLarge
            it.setAnimClick(anim, AnimState.End) {
                findNavController().navigate(R.id.action_introductionFragment_to_loginFragment)
            }
        }
    }
}