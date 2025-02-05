package mai.project.foodmap.data.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai.project.foodmap.data.annotations.StatusCode
import mai.project.foodmap.data.remoteDataSource.models.BaseResponse
import mai.project.foodmap.domain.models.NetworkResult
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
 * 將 NetworkResult 結果 轉換為 Nothing
 */
internal fun <T> NetworkResult<T>.mapToNothing(): NetworkResult<Nothing> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(null)
    is NetworkResult.Error -> NetworkResult.Error(message, null)
    is NetworkResult.Loading -> NetworkResult.Loading()
    is NetworkResult.Idle -> NetworkResult.Idle()
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
                    body is BaseResponse && body.status == StatusCode.SUCCESS ->
                        NetworkResult.Success(body)

                    body is BaseResponse && body.status != StatusCode.SUCCESS ->
                        NetworkResult.Error(message = body.errMsg ?: "Unknown Error", body)

                    else -> NetworkResult.Success(body)
                }
            }

            else -> {
                Timber.e(message = response.message())
                NetworkResult.Error(response.message())
            }
        }
    } catch (e: Exception) {
        Timber.e(message = "API response failed", t = e)
        NetworkResult.Error(e.message ?: "Unknown Error")
    }
}