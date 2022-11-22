package com.side.project.foodmap.helper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatImageView
import coil.imageLoader
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.side.project.foodmap.util.Method

fun View.displayErrorShortSnackBar(message: String) =
    Snackbar.make(this, "Error：${message}", Snackbar.LENGTH_SHORT).show()

fun View.displayErrorLongSnackBar(message: String) =
    Snackbar.make(this, "Error：${message}", Snackbar.LENGTH_LONG).show()

fun View.getString(stringResId: Int): String
    = resources.getString(stringResId)

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hidden() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun AppCompatImageView.loadFromGoogle(photo_reference: String) {
    Method.logE("PHOTO", photo_reference)
    this.load(
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=" +
                "$photo_reference&key=${this.context.appInfo().metaData["GOOGLE_KEY"].toString()}",
        imageLoader = this.context.imageLoader
    )
}