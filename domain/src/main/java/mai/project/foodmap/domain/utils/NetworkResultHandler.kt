package mai.project.foodmap.domain.utils

import mai.project.foodmap.domain.state.NetworkResult

/**
 * 封裝 API Response 結果的處理
 */
class ResultHandler<T> {
    var onSuccess: (T?) -> Unit = {}
    var onError: (T?, String?) -> Unit = { _, _ -> }
    var onAccessKeyIllegal: () -> Unit = {}
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
        is NetworkResult.AccessKeyIllegal -> handler.onAccessKeyIllegal()
        is NetworkResult.Loading -> handler.onLoading()
        is NetworkResult.Idle -> handler.onIdle()
    }
}
