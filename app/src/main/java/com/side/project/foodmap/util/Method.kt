package com.side.project.foodmap.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.side.project.foodmap.util.Constants.PERMISSION_CODE

object Method {
    /**
     * Logcat
     */
    fun logE(tag: String, message: String) =
        Log.e(tag, message)

    fun logD(tag: String, message: String) =
        Log.d(tag, message)

    /**
     * Tools
     */
    fun showKeyBoard(activity: AppCompatActivity, ed: EditText) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(ed, 0)
    }

    fun hideKeyBoard(activity: AppCompatActivity) {
        activity.currentFocus?.let {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun hideKeyBoard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Permissions
     */
    fun requestPermission(activity: Activity, vararg permissions: String): Boolean {
        return if (!hasPermissions(activity, *permissions)) {
            requestPermissions(activity, permissions, PERMISSION_CODE)
            false
        } else
            true
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions)
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false
        return true
    }
}