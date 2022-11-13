package com.side.project.foodmap.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.firebase.messaging.FirebaseMessaging
import com.side.project.foodmap.R
import com.side.project.foodmap.util.Constants.PERMISSION_CODE
import java.io.ByteArrayOutputStream
import java.lang.Exception

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