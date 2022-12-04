package com.side.project.foodmap.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessaging
import com.side.project.foodmap.BuildConfig
import com.side.project.foodmap.R
import com.side.project.foodmap.helper.getLocation
import com.side.project.foodmap.util.Constants.PERMISSION_CODE
import java.io.ByteArrayOutputStream
import java.util.*

object Method {
    /**
     * Logcat
     */
    fun logE(tag: String, message: String) {
        if (BuildConfig.DEBUG)
            Log.e(tag, message)
    }

    fun logD(tag: String, message: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, message)
    }

    /**
     * Tools
     */
    fun getFcmToken(work: (String) -> Unit) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    logE("FCM Token", "Get success.")
                    work(token)
                }.addOnFailureListener {
                    logE("FCM Token", "Get failed.")
                    work("")
                }
        } catch (e: Exception) {
            logE("FCM Token", "Get crash.")
            work("")
        }
    }

    fun decodeImage(encodedImage: String?): Bitmap? {
        return if (encodedImage != null) {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else
            null
    }

    fun encodeImage(bitmap: Bitmap): String? {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.height
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)

        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun getCurrentLatLng(region: String, latLng: LatLng): LatLng =
        LatLng(
            if (region.getLocation().first != 0.00) region.getLocation().first else latLng.latitude,
            if (region.getLocation().second != 0.00) region.getLocation().second else latLng.longitude
        )

    fun getWeekOfDate(dt: Date): Int {
        val weekDays = arrayOf(7, 1, 2, 3, 4, 5, 6)
        val cal = Calendar.getInstance()
        cal.time = dt
        var w = cal[Calendar.DAY_OF_WEEK] - 1
        if (w < 0)
            w = 0
        return weekDays[w]
    }

    fun createMapIcon(activity: Activity, view: View): Bitmap {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
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
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            )
                return false
        return true
    }

    /**
     * Verify register/login
     * 參考：https://regexr.com/6hpe0
     */
    fun validateEmail(email: String): RegisterLoginValidation {
        if (email.isEmpty())
            return RegisterLoginValidation.Failed(R.string.hint_email_is_empty)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return RegisterLoginValidation.Failed(R.string.hint_email_wrong_format)

        return RegisterLoginValidation.Success
    }

    fun validateAccount(account: String): RegisterLoginValidation {
        if (account.isEmpty())
            return RegisterLoginValidation.Failed(R.string.hint_account_is_empty)
        if (account.length < 6)
            return RegisterLoginValidation.Failed(R.string.hint_account_less_char)
        if ((!account.matches(Regex(".*[a-z]+.*")) && !account.matches(Regex(".*[A-Z]+.*")))
            || !account.matches(Regex(".*[0-9]+.*"))
        ) return RegisterLoginValidation.Failed(R.string.hint_account_not_format)

        return RegisterLoginValidation.Success
    }

    fun validatePassword(password: String): RegisterLoginValidation {
        if (password.isEmpty())
            return RegisterLoginValidation.Failed(R.string.hint_password_is_empty)
        if (password.length < 6)
            return RegisterLoginValidation.Failed(R.string.hint_password_less_char)
        if (!password.matches(Regex(".*[a-z]+.*")) && !password.matches(Regex(".*[A-Z]+.*")))
            return RegisterLoginValidation.Failed(R.string.hint_password_less_alphabet)

        return RegisterLoginValidation.Success
    }
}