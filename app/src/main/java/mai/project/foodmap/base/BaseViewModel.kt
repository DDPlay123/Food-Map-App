package mai.project.foodmap.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mai.project.core.utils.CoroutineContextProvider
import timber.log.Timber

/**
 * 基礎 ViewModel ，用於繼承
 */
abstract class BaseViewModel(
    open val contextProvider: CoroutineContextProvider
) : ViewModel() {

    /**
     * 執行工作的 Job
     */
    private val job: Job = SupervisorJob()

    /**
     * 處理 Job 的 Exception
     *
     * - 預設會 log 出 Exception
     */
    open val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
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