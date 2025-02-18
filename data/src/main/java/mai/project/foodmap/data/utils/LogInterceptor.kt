package mai.project.foodmap.data.utils

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSource
import timber.log.Timber
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.Locale

/**
 * 攔截 HTTP API 的 Request & Response 在 Logcat 中顯示
 */
internal class LogInterceptor : Interceptor {
    private val utf8: Charset = Charset.forName("UTF-8")
    private var timeReq: Long = 0L
    private var timeResp: Long = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        timeReq = System.nanoTime()

        val reqBody: String? = interceptRequestBody(request)

        logRequest(request, reqBody)

        val response: Response = chain.proceed(request)
        val respBody: String = interceptResponseBody(response)
        timeResp = System.nanoTime()

        logResponse(response, respBody)

        return response
    }

    /**
     * 攔截 Request Body
     */
    private fun interceptRequestBody(request: Request): String? {
        return request.body?.let { requestBody ->
            val buffer = okio.Buffer()
            requestBody.writeTo(buffer)

            val contentType: MediaType? = requestBody.contentType()
            val charset = contentType?.charset(utf8) ?: utf8

            buffer.readString(charset)
        }
    }

    /**
     * 攔截 Response Body
     */
    private fun interceptResponseBody(response: Response): String {
        return response.body.let { responseBody ->
            val source: BufferedSource? = responseBody?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer

            var charset: Charset = utf8
            val contentType: MediaType? = responseBody?.contentType()
            contentType?.let {
                try {
                    charset = contentType.charset(utf8) ?: utf8
                } catch (e: UnsupportedCharsetException) {
                    Timber.e(message = "interceptResponseBody", t = e)
                }
            }
            buffer?.clone()?.readString(charset) ?: ""
        }
    }

    /**
     * Logcat Request
     */
    private fun logRequest(request: Request, reqBody: String?) {
        Timber.d(
            message = "\nRequest:\nmethod:${request.method}\nURL:${request.url}\nheaders:${request.headers}\nbody:$reqBody"
        )
    }

    /**
     * Logcat Response
     */
    private fun logResponse(response: Response, respBody: String?) {
        // 計算時間差，單位為毫秒（ms）
        val elapsedTime = if (timeResp != 0L && timeReq != 0L) {
            String.format(Locale.TAIWAN, "%.1f", (timeResp - timeReq) / 1e6)
        } else {
            "N/A"
        }

        Timber.d(
            message = "\nResponse:\ncode:${response.code}\nTime：${elapsedTime}ms\nURL:${response.request.url}\nbody:$respBody"
        )
    }
}