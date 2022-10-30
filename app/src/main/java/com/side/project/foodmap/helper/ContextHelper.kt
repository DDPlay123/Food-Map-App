package com.side.project.foodmap.helper

import android.content.Context
import android.widget.Toast

fun Context.displayShortToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.displayLongToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()