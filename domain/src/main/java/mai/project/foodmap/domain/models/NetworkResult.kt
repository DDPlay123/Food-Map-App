package mai.project.foodmap.domain.models

/**
 * 封裝 API Response 的結果
 *
 * - Success: 成功
 * - Error: 失敗
 * - Loading: 載入中
 * - Idle: 空閒
 *
 * @param T API Response
 * @param message 錯誤訊息
 */
sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T?) : NetworkResult<T>(data)
    class Error<T>(message: String? = null, data: T? = null) : NetworkResult<T>(data, message)
    class Loading<T> : NetworkResult<T>()
    class Idle<T> : NetworkResult<T>()
}

