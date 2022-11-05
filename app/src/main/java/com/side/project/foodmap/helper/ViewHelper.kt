package com.side.project.foodmap.helper

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.displayErrorShortSnackBar(message: String) =
    Snackbar.make(this, "Error：${message}", Snackbar.LENGTH_SHORT).show()

fun View.displayErrorLongSnackBar(message: String) =
    Snackbar.make(this, "Error：${message}", Snackbar.LENGTH_LONG).show()

fun View.display() {
    this.visibility = View.VISIBLE
}

fun View.hidden() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}