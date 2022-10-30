package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.side.project.foodmap.ui.activity.BaseActivity
import com.side.project.foodmap.ui.other.DialogManager

abstract class BaseFragment: Fragment() {
    lateinit var mActivity: BaseActivity
    lateinit var dialog: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mActivity = activity as BaseActivity
        dialog = mActivity.dialog
        super.onCreate(savedInstanceState)
    }
}