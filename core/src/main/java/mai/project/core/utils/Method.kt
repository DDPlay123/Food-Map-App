package mai.project.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * 常用或通用的方法
 */
object Method {

    /**
     * 將 base64 字串轉換成 bitmap
     */
    fun decodeImage(encodedImage: String?): Bitmap? {
        return if (encodedImage != null) {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else
            null
    }

    /**
     * 將 bitmap 轉換成 base64 字串
     */
    fun encodeImage(bitmap: Bitmap): String? {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.height
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)

        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}