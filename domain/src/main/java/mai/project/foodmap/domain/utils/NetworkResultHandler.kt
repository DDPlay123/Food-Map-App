package mai.project.foodmap.domain.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import mai.project.foodmap.domain.models.NetworkResult
import timber.log.Timber

/**
 * 封裝 API Response 結果的處理
 */
class ResultHandler<T> {
    var onSuccess: (T?) -> Unit = {}
    var onError: (T?, String?) -> Unit = { _, _ -> }
    var onLoading: () -> Unit = {}
    var onIdle: () -> Unit = {}
}

/**
 * 處理 API Response 的結果
 */
inline fun <T> NetworkResult<T>.handleResult(
    handlerBlock: ResultHandler<T>.() -> Unit
) {
    val handler = ResultHandler<T>().apply(handlerBlock)

    when (this) {
        is NetworkResult.Success -> handler.onSuccess(data)
        is NetworkResult.Error -> handler.onError(data, message)
        is NetworkResult.Loading -> handler.onLoading()
        is NetworkResult.Idle -> handler.onIdle()
    }
}

/**
 * 安全的 API 呼叫方式
 *
 * @param apiCall API 呼叫
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> NetworkResult<T>
): NetworkResult<T> {
    return try {
        apiCall.invoke()
    } catch (e: Exception) {
        Timber.e(message = "API call failed", t = e)
        FirebaseCrashlytics.getInstance().recordException(
            Exception("API call failed on ViewModel", e)
        )
        NetworkResult.Error(message = "Network Error")
    }
}