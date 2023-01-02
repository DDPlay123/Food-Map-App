package com.side.project.foodmap.ui.fragment.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.DialogManager

open class BaseFragment<VB : ViewDataBinding>(@LayoutRes val layoutRes: Int) : Fragment() {
    private var _binding: VB? = null
    val binding : VB?
        get() = _binding

    lateinit var mActivity: BaseActivity
    lateinit var animManager: AnimManager
    lateinit var dialog: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mActivity = activity as BaseActivity
        animManager = mActivity.animManager
        dialog = mActivity.dialog
        super.onCreate(savedInstanceState)
    }

    open fun VB.initialize() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)

        binding?.lifecycleOwner = viewLifecycleOwner
        binding?.initialize()

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog.cancelAllDialog()
        _binding = null
    }
}