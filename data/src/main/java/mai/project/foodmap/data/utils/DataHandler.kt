package mai.project.foodmap.data.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai.project.foodmap.data.annotations.StatusCode
import mai.project.foodmap.data.remoteDataSource.models.BaseResponse
import mai.project.foodmap.domain.state.NetworkResult
import retrofit2.Response
import timber.log.Timber

/**
 * 安全執行 IO 工作
 */
internal suspend fun safeIoWorker(
    work: suspend () -> Unit
) {
    return withContext(Dispatchers.IO) {
        try {
            work()
        } catch (e: Exception) {
            Timber.e(message = "IO operate failed", t = e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}

/**
 * 將 NetworkResult 轉換為其他型態
 */
internal fun <T, R> NetworkResult<T>.mapResult(transform: (T?) -> R?): NetworkResult<R> {
    val result = transform(data)
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(result)
        is NetworkResult.Error -> NetworkResult.Error(message, result)
        is NetworkResult.AccessKeyIllegal -> NetworkResult.AccessKeyIllegal()
        is NetworkResult.Loading -> NetworkResult.Loading()
        is NetworkResult.Idle -> NetworkResult.Idle()
    }
}

/**
 * 處理 API Response
 *
 * @param response [Response]
 */
internal fun <T> handleAPIResponse(
    response: Response<T>
): NetworkResult<T> {
    return try {
        when {
            // Timeout Error (屬於使用者個人的問題)
            response.message().toString().contains("timeout") -> {
                Timber.e(message = response.message())
                NetworkResult.Error(message = "Timeout")
            }

            // 成功接收資料後的處理
            response.isSuccessful -> {
                val body = response.body()
                when {
                    body == null -> NetworkResult.Error(message = "Unknown Error")

                    body is BaseResponse && body.status == StatusCode.SUCCESS ->
                        NetworkResult.Success(body)

                    body is BaseResponse && body.status == StatusCode.ACCESS_KEY_ERROR ->
                        NetworkResult.AccessKeyIllegal()

                    body is BaseResponse && body.status != StatusCode.SUCCESS ->
                        NetworkResult.Error(message = body.errMsg ?: "Unknown Error", body)

                    else -> NetworkResult.Success(body)
                }
            }

            else -> {
                // 處理 HTTP 錯誤代碼
                val errorMessage = when (response.code()) {
                    in 400..499 -> "Client Error ${response.code()}: ${response.message()}"
                    in 500..599 -> "Server Error ${response.code()}: ${response.message()}"
                    else -> "Unexpected Error ${response.code()}: ${response.message()}"
                }
                Timber.e("API Error: $errorMessage")
                NetworkResult.Error(errorMessage)
            }
        }
    } catch (e: Exception) {
        Timber.e(message = "API response failed", t = e)
        NetworkResult.Error(e.message ?: "Unknown Error")
    }
}