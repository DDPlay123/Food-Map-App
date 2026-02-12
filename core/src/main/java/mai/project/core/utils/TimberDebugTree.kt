package mai.project.core.utils

import timber.log.Timber

/**
 * Timber Debug Tree
 */
class TimberDebugTree : Timber.DebugTree() {
    companion object {
        private const val MAX_LOG_LENGTH = 4000 // Log 的長度限制為 4096，改用 4000 作為最大值
        private const val CHUNK_SIZE = 3500     // 每個 Log 分塊的長度，3500 作為緩衝
    }

    override fun createStackElementTag(element: StackTraceElement): String =
        "[${element.fileName}:${element.lineNumber}#${element.methodName}]"

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when {
            message.length <= MAX_LOG_LENGTH ->
                super.log(priority, tag, message, t)

            else -> {
                val chunks = message.splitToChunks(CHUNK_SIZE)
                chunks.forEachIndexed { index, chunk ->
                    val prefix = if (chunks.size > 1) "[${index + 1}/${chunks.size}] " else ""
                    super.log(priority, tag, prefix + chunk, null)
                }
                t?.let { super.log(priority, tag, "", it) }
            }
        }
    }

    /**
     * 將訊息本身依照 [chunkSize] 拆分
     *
     * - Timber 有字數限制
     */
    private fun String.splitToChunks(chunkSize: Int): List<String> {
        if (length <= chunkSize) return listOf(this)

        val chunks = mutableListOf<String>()
        var startIndex = 0

        while (startIndex < length) {
            var endIndex = (startIndex + chunkSize).coerceAtMost(length)

            if (endIndex < length) {
                val lastNewline = lastIndexOf('\n', endIndex)
                if (lastNewline > startIndex) {
                    endIndex = lastNewline + 1
                }
            }

            chunks.add(substring(startIndex, endIndex))
            startIndex = endIndex
        }

        return chunks
    }
}