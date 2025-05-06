package mai.project.foodmap.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import mai.project.core.utils.CoroutineContextProvider
import mai.project.foodmap.GlobalEvent
import mai.project.foodmap.domain.state.NetworkResult
import timber.log.Timber

/**
 * 基礎 ViewModel ，用於繼承
 */
abstract class BaseViewModel(
    open val contextProvider: CoroutineContextProvider
) : ViewModel() {

    /**
     * 安全的 API 呼叫方式
     *
     * @param apiCall API 呼叫
     */
    protected fun <T> safeApiCallFlow(
        apiCall: suspend () -> NetworkResult<T>
    ): Flow<NetworkResult<T>> = flow {
        // 先發射 Loading 狀態
        emit(NetworkResult.Loading())

        // 呼叫 API
        val result = try {
            apiCall.invoke().apply {
                GlobalEvent.setAccessKeyIllegal(this is NetworkResult.AccessKeyIllegal)
            }
        } catch (e: Exception) {
            Timber.e(t = e, message = "API call failed")
            FirebaseCrashlytics.getInstance().recordException(
                Exception("API call failed on ViewModel", e)
            )
            NetworkResult.Error(message = "Network Error") // 失敗時回傳 Error
        }

        // 發射最終結果 (Success or Error)
        emit(result)
    }

    /**
     * 是否正在載入中 (通常用於 API 請求)
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun setLoading(
        isLoading: Boolean,
        timeoutMillis: Long = 30000L
    ) = launchCoroutineDefault {
        _isLoading.value = isLoading
        if (isLoading) {
            try {
                withTimeout(timeoutMillis) { delay(timeoutMillis) }
            } catch (_: TimeoutCancellationException) {
                _isLoading.value = false
            }
        }
    }

    /**
     * 執行工作的 Job
     */
    private val job: Job = SupervisorJob()

    /**
     * 處理 Job 的 Exception
     *
     * - 預設會 log 出 Exception
     */
    protected open val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    /**
     * 執行 IO 的 Coroutine
     *
     * @param block [suspend CoroutineScope.() -> Unit] Coroutine 內容
     * @return [Job] Coroutine Job
     */
    protected fun launchCoroutineIO(
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(contextProvider.io + job + coroutineExceptionHandler) {
        block.invoke(this)
    }

    /**
     * 執行 Main 的 Coroutine
     *
     * @param block [suspend CoroutineScope.() -> Unit] Coroutine 內容
     * @return [Job] Coroutine Job
     */
    protected fun launchCoroutineMain(
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(contextProvider.main + job + coroutineExceptionHandler) {
        block.invoke(this)
    }

    /**
     * 執行 Default 的 Coroutine
     *
     * @param block [suspend CoroutineScope.() -> Unit] Coroutine 內容
     * @return [Job] Coroutine Job
     */
    protected fun launchCoroutineDefault(
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(contextProvider.default + job + coroutineExceptionHandler) {
        block.invoke(this)
    }

    init {
        Timber.d(message = "${this::class.simpleName} initialized")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d(message = "${this::class.simpleName} cleared")
        job.cancel()
    }
}