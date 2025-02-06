package mai.project.core.utils

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

/**
 * 避免在界面快速切換或短暫地取消訂閱時，Flow 被馬上停掉，但又被很快被重新啟動所帶來的閃爍或 overhead
 *
 * @see <a href="https://blog.p-y.wtf/whilesubscribed5000">來源</a>
 */
data object WhileSubscribedOrRetained : SharingStarted {

    private val handler = Handler(Looper.getMainLooper())

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> = subscriptionCount
        // 查看訂閱者數量
        .transformLatest { count ->
            withContext(Dispatchers.Main.immediate) {
                if (count > 0) {
                    // 當訂閱者出現時，開始蒐集
                    emit(SharingCommand.START)
                } else {
                    // 如果沒有訂閱者，等到下一幀時停止蒐集
                    val posted = CompletableDeferred<Unit>()

                    // 使用 Choreographer 排程了一次畫面更新(frame callback)後，再真正停止。
                    Choreographer.getInstance().postFrameCallback {
                        // 包兩層是為了確保畫面這一幀的後續任務、以及可能的 UI 排程，都已執行完畢
                        handler.postAtFrontOfQueue {
                            handler.post {
                                posted.complete(Unit)
                            }
                        }
                    }
                    posted.await()
                    emit(SharingCommand.STOP)
                }
            }
        }
        // 確保只有從 STOP → START 或 START → STOP 之類的狀態改變才會真正送出
        .dropWhile { it != SharingCommand.START }
        .distinctUntilChanged()
}
