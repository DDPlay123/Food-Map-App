package com.side.project.foodmap.util

import android.os.Build
import androidx.annotation.RequiresApi

object Constants {
    /**
     * Other
     */
    const val MMSLAB = "MMSLAB" // AES KEY
    const val DIALOG_ALBUM = "DIALOG_ALBUM"
    const val ALBUM_IMAGE_RESOURCE = "ALBUM_IMAGE_RESOURCE"
    const val IMAGE_RESOURCE = "IMAGE_RESOURCE" // Album Image Key
    const val IMAGE_POSITION = "IMAGE_POSITION"

    /**
     * Permission
     */
    @RequiresApi(33)
    const val PERMISSION_MEDIA_IMAGES = android.Manifest.permission.READ_MEDIA_IMAGES
    const val PERMISSION_CAMERA = android.Manifest.permission.CAMERA
    const val PERMISSION_RECORD_AUDIO = android.Manifest.permission.RECORD_AUDIO
    const val PERMISSION_WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PERMISSION_READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE
    const val PERMISSION_FINE_LOCATION =  android.Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_COARSE_LOCATION =  android.Manifest.permission.ACCESS_COARSE_LOCATION

    val location_permission = arrayOf(PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION)
    val download_permission = arrayOf(PERMISSION_WRITE_EXTERNAL_STORAGE)
    val camera_permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(PERMISSION_MEDIA_IMAGES, PERMISSION_CAMERA)
    else
        arrayOf(PERMISSION_CAMERA, PERMISSION_WRITE_EXTERNAL_STORAGE, PERMISSION_READ_EXTERNAL_STORAGE)
    val audio_permission = arrayOf(PERMISSION_RECORD_AUDIO)

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
    const val HISTORY_SEARCH = "HISTORY_SEARCH"

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
    const val IS_FAVORITE = "IS_FAVORITE" // For Result Value

    // List Activity
    const val KEYWORD = "KEYWORD"
    const val DISTANCE = "DISTANCE"
    const val IS_NEAR_SEARCH = "IS_NEAR_SEARCH"
    const val LATITUDE = "LATITUDE"
    const val LONGITUDE = "LONGITUDE"
}