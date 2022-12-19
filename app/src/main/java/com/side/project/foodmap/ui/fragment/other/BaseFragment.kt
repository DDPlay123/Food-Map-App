package com.side.project.foodmap.ui.fragment.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.DialogManager
import com.side.project.foodmap.util.Constants.camera_permission
import com.side.project.foodmap.util.Constants.location_permission
import com.side.project.foodmap.util.tools.Method
import org.koin.android.ext.android.inject

open class BaseFragment<T : ViewDataBinding>(@LayoutRes val layoutRes: Int) : Fragment() {
    private var _binding: T? = null
    val binding : T get() = _binding!!
    val animManager: AnimManager by inject()

    lateinit var mActivity: BaseActivity
    lateinit var dialog: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mActivity = activity as BaseActivity
        dialog = mActivity.dialog
        super.onCreate(savedInstanceState)
    }

    open fun T.initialize() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.initialize()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun requestLocationPermission(): Boolean {
        if (!Method.requestPermission(mActivity, *location_permission)) {
            mActivity.displayShortToast(getString(R.string.hint_not_location_permission))
            return false
        }
        return true
    }

    fun requestCameraPermission(): Boolean {
        if (!Method.requestPermission(mActivity, *camera_permission)) {
            mActivity.displayShortToast(getString(R.string.hint_not_camera_permission))
            return false
        }
        return true
    }
}