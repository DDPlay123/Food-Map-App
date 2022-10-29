package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.navigation.fragment.findNavController
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentIntroductionBinding
import com.side.project.foodmap.util.AnimManager
import org.koin.android.ext.android.inject

class IntroductionFragment : Fragment() {
    private var _binding: FragmentIntroductionBinding? = null
    private val binding get() = _binding!!

    private val animManager: AnimManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroductionBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
    }

    private fun setListener() {
        binding.run {
            val anim = animManager.smallToLarge
            btnNext.setOnClickListener {
                it.startAnimation(anim)
            }
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    findNavController().navigate(R.id.action_introductionFragment_to_loginFragment)
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }
    }
}