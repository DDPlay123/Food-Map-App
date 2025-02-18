package mai.project.foodmap.features.auth_features.authScreen

import mai.project.foodmap.domain.state.ValidationResult

/**
 * 驗證欄位狀態
 *
 * @param username 驗證帳號
 * @param password 驗證密碼
 */
data class AuthFieldsState(
    val username: ValidationResult? = null,
    val password: ValidationResult? = null
)