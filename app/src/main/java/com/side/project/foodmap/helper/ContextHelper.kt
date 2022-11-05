package com.side.project.foodmap.helper

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast

fun Context.displayShortToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.displayLongToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Context.appInfo(): ApplicationInfo =
    this.packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)