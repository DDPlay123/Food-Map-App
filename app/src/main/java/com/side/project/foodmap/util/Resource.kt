package com.side.project.foodmap.util

// 用於觀察資料流狀態
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T, message: String = ""): Resource<T>(data, message)
    class Error<T>(message: String): Resource<T>(message = message)
    class Loading<T>: Resource<T>()
    class Unspecified<T>: Resource<T>()
}