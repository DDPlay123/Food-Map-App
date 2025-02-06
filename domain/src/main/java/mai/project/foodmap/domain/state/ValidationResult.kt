package mai.project.foodmap.domain.state

import androidx.annotation.StringRes

/**
 * 封裝簡易的驗證結果
 *
 * - Success: 驗證成功
 * - Error: 驗證失敗
 */
sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Failure(@StringRes val stringRes: Int) : ValidationResult()
}
