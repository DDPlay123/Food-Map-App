package mai.project.foodmap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mai.project.core.utils.Event

/**
 * 全域事件
 *
 * - 用於傳遞全域事件，例如：AccessKey 錯誤...
 */
object GlobalEvent {

    /**
     * 傳遞 AccessKey 錯誤
     */
    private val _accessKeyIllegalChannel = Channel<Event<Boolean>>(Channel.CONFLATED)
    val accessKeyIllegal = _accessKeyIllegalChannel.receiveAsFlow()

    fun setAccessKeyIllegal(isIllegal: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            _accessKeyIllegalChannel.send(Event(isIllegal))
        }
    }
}