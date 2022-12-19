package com.side.project.foodmap.util

import android.os.Build
import androidx.annotation.RequiresApi

object Constants {
    /**
     * AES Key
     */
    const val MMSLAB = "MMSLAB"
    /**
     * Permission
     */
    @RequiresApi(33)
    const val PERMISSION_MEDIA_IMAGES = android.Manifest.permission.READ_MEDIA_IMAGES
    const val PERMISSION_CAMERA = android.Manifest.permission.CAMERA
    const val PERMISSION_WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PERMISSION_READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE
    const val PERMISSION_FINE_LOCATION =  android.Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_COARSE_LOCATION =  android.Manifest.permission.ACCESS_COARSE_LOCATION

    val location_permission = arrayOf(PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION)
    val camera_permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(PERMISSION_MEDIA_IMAGES, PERMISSION_CAMERA, PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE)
    else
        arrayOf(PERMISSION_CAMERA, PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE)

    /**
     * Permission Code
     */
    const val PERMISSION_CODE = 1001

    /**
     * Firebase
     */
    const val USER_COLLECTION = "User"

    /**
     * User Room Database
     */
    const val DISTANCE_SEARCH_MODEL = "DISTANCE_SEARCH_MODEL"
    const val DRAW_CARD_MODEL = "DRAW_CARD_MODEL"
    const val GET_FAVORITE_MODEL = "GET_FAVORITE_MODEL"

    /**
     * User Preference
     */
    const val USER_PUBLIC_INFO = "USER_PUBLIC_INFO"
    const val USER_ACCOUNT = "USER_ACCOUNT"
    const val USER_PASSWORD = "USER_PASSWORD"

    const val USERS_PREFERENCE = "USER_INFO"
    const val USER_ACCESS_KEY = "USER_ACCESS_KEY"
    const val USER_DEVICE_ID = "USER_DEVICE_ID"
    const val USER_UID = "USER_UID"
    const val USER_REGION = "USER_REGION"
    const val USER_NAME = "USER_NAME"
    const val USER_PICTURE = "USER_PICTURE"
    const val USER_IS_LOGIN = "USER_IS_LOGIN"
    const val USER_TDX_TOKEN = "USER_TDX_TOKEN"
    const val USER_TDX_TOKEN_UPDATE = "USER_TDX_TOKEN_UPDATE"

    /**
     * Activity Parameter
     */
    // Detail Activity
    const val PLACE_ID = "PLACE_ID"

    // List Activity
    const val KEYWORD = "KEYWORD"
    const val IS_NEAR_SEARCH = "IS_NEAR_SEARCH"
    const val LATITUDE = "LATITUDE"
    const val LONGITUDE = "LONGITUDE"
}