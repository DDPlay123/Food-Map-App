package com.side.project.foodmap.ui.fragment

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
import com.side.project.foodmap.service.LocationService
import com.side.project.foodmap.ui.activity.BaseActivity
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.DialogManager
import com.side.project.foodmap.util.Constants.PERMISSION_COARSE_LOCATION
import com.side.project.foodmap.util.Constants.PERMISSION_FINE_LOCATION
import org.koin.android.ext.android.inject

open class BaseFragment<T : ViewDataBinding>(@LayoutRes val layoutRes: Int) : Fragment() {
    private var _binding: T? = null
    val binding : T get() = _binding!!
    val animManager: AnimManager by inject()

    private lateinit var locationService: LocationService
    var myLatitude: Double = DEFAULT_LATITUDE
    var myLongitude: Double = DEFAULT_LONGITUDE

    lateinit var mActivity: BaseActivity
    lateinit var dialog: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mActivity = activity as BaseActivity
        dialog = mActivity.dialog
        super.onCreate(savedInstanceState)
    }

    open fun T.initialize() {}

    fun initLocationService() {
        locationService = LocationService()
        locationService.startListener(mActivity)
        if (!locationService.canGetLocation()) {
            mActivity.displayShortToast(getString(R.string.hint_not_provider_gps))
            return
        }
        locationService.latitude.observe(viewLifecycleOwner) { myLatitude = it }
        locationService.longitude.observe(viewLifecycleOwner) { myLongitude = it }
    }

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
        if (::locationService.isInitialized)
            locationService.stopListener(mActivity)
        _binding = null
    }

    companion object {
        val permission = arrayOf(PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION)
        private const val DEFAULT_LATITUDE = 25.043871531367014
        private const val DEFAULT_LONGITUDE = 121.53453374432904
    }
}