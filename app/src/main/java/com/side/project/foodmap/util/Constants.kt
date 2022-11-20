package com.side.project.foodmap.util

object Constants {
    /**
     * Permission
     */
    const val PERMISSION_CAMERA = android.Manifest.permission.CAMERA
    const val PERMISSION_FINE_LOCATION =  android.Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_COARSE_LOCATION =  android.Manifest.permission.ACCESS_COARSE_LOCATION

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
}