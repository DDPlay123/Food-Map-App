package com.side.project.foodmap.util

// 用於觀察驗證註冊/登入狀態
sealed class RegisterLoginValidation {
    object Success: RegisterLoginValidation()
    data class Failed(val messageID: Int): RegisterLoginValidation()
}

data class RegisterLoginFieldsState(
    val username: RegisterLoginValidation? = null,
    val password: RegisterLoginValidation? = null
)