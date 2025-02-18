package mai.project.core.utils

import kotlinx.coroutines.CoroutineDispatcher

/**
 * 確保統一調度器
 */
interface CoroutineContextProvider {
    /**
     * IO 調度器
     */
    val io: CoroutineDispatcher

    /**
     * Main 調度器
     */
    val main: CoroutineDispatcher

    /**
     * 預設調度器
     */
    val default: CoroutineDispatcher
}
