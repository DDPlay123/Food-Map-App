package com.side.project.foodmap.helper

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.inputmethod.InputMethodManager
import com.side.project.foodmap.R
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.tools.Method

fun Activity.requestLocationPermission(): Boolean {
    if (!Method.requestPermission(this, *Constants.location_permission)) {
        displayShortToast(getString(R.string.hint_not_location_permission))
        return false
    }
    return true
}

fun Activity.requestCameraPermission(): Boolean {
    if (!Method.requestPermission(this, *Constants.camera_permission)) {
        displayShortToast(getString(R.string.hint_not_camera_permission))
        return false
    }
    return true
}

fun Activity.requestAudioPermission(): Boolean {
    if (!Method.requestPermission(this, *Constants.audio_permission)) {
        displayShortToast(getString(R.string.hint_not_audio_permission))
        return false
    }
    return true
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}

fun Activity.hideSoftKeyboard() {
    if (currentFocus != null) {
        val inputMethodManager = getSystemService(Context
            .INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }
}

fun Activity.isKeyboardVisible(): Boolean {
    val r = Rect()

    // r will be populated with the coordinates of your view that area still visible.
    window.decorView.getWindowVisibleDisplayFrame(r)

    // get screen height and calculate the difference with the usable area from the r
    val height = getDisplaySize().y
    val diff = height - r.bottom

    // If the difference is not 0 we assume that the keyboard is currently visible.
    return diff != 0
}

fun Activity.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0)
        result = resources.getDimensionPixelSize(resourceId)

    return result
}

fun Activity.getNavigationBarHeight(): Int {
    var result = 0
    val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0)
        result = resources.getDimensionPixelSize(resourceId)

    return result
}