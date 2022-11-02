package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.ui.viewModel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        checkTdxToken() // 暫時棄用 TDX API
    }

    private fun checkTdxToken() {
        viewModel.getUserTdxTokenUpdate()
        viewModel.userTdxTokenUpdate.observe(viewLifecycleOwner) { oldDate ->
            val todayDate: String = SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).format(Date())
            if (todayDate > oldDate)
                viewModel.updateTdxToken(todayDate)
        }
    }
}