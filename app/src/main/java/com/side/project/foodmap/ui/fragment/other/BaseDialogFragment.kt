package com.side.project.foodmap.ui.fragment.other

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.getColorCompat
import com.side.project.foodmap.ui.activity.other.BaseActivity
import com.side.project.foodmap.ui.other.AnimManager
import org.koin.android.ext.android.inject

open class BaseDialogFragment<T : ViewDataBinding>(@LayoutRes val layoutRes: Int) : DialogFragment() {
    private var _binding: T? = null
    val binding : T get() = _binding!!
    val animManager: AnimManager by inject()

    lateinit var mActivity: BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as BaseActivity
        setStyle(STYLE_NORMAL, R.style.BaseDialogStyle)
    }

    override fun onStart() {
        super.onStart()
        // 全版面
        val window: Window? = requireDialog().window
        window?.apply {
            val lp: WindowManager.LayoutParams = attributes
            lp.gravity = Gravity.CENTER
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes = lp
            setBackgroundDrawable(ColorDrawable(requireContext().getColorCompat(R.color.black)))
        }
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
}