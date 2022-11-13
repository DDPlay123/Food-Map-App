package com.side.project.foodmap.helper

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat

fun Context.displayShortToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.displayLongToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Context.appInfo(): ApplicationInfo =
    this.packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)

fun Context.getColorCompat(color: Int) =
    ContextCompat.getColor(this, color)

fun Context.getDrawableCompat(drawableResId: Int): Drawable? =
    ContextCompat.getDrawable(this, drawableResId)

fun Context.getWindowManager() =
    getSystemService(Context.WINDOW_SERVICE) as WindowManager

fun Context.getDisplaySize() = Point().apply {
    getWindowManager().defaultDisplay.getSize(this)
}