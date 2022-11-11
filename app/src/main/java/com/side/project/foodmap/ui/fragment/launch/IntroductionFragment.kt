package com.side.project.foodmap.ui.fragment.launch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentIntroductionBinding
import com.side.project.foodmap.helper.setAnimClick
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.AnimState
import org.koin.android.ext.android.inject

class IntroductionFragment : Fragment() {
    private var _binding: FragmentIntroductionBinding? = null
    private val binding get() = _binding!!

    private val animManager: AnimManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_introduction, container, false)
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
                it.setAnimClick(anim, AnimState.End) {
                    findNavController().navigate(R.id.action_introductionFragment_to_loginFragment)
                }
            }
        }
    }
}