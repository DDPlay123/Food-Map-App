package mai.project.core.utils

/**
 * One-Time Events
 *
 * 主要用於觀察資料時，只需要觀察一次的情況。
 *
 * ```
 * 例如：
 * 當 Fragment 從堆疊中返回時，它會重新訂閱 LiveData 或 StateFlow，並收到最新的值。
 * 這會導致如果你的觀察者是根據這些值來執行某些動作（例如導航或顯示 Toast），那麼這些動作可能會被重複執行。
 */
open class Event<out T>(private val content: T) {

    /**
     * 如果事件已被處理，則為 true，否則為 false。
     */
    var hasBeenHandled = false
        private set

    /**
     * 如果該狀態還未執行，則執行。否則不執行。
     */
    val getContentIfNotHandled: T?
        get() {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

    /**
     * 無論如何都會執行。
     */
    val getPeekContent: T = content
}