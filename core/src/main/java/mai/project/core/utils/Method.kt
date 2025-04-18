package mai.project.core.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import mai.project.core.Configs
import java.io.ByteArrayOutputStream
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * 常用或通用的方法
 */
object Method {

    /**
     * 請求權限
     */
    fun requestPermission(
        activity: Activity,
        vararg permissions: String
    ): Boolean {
        val hasPermissions = permissions.all {
            activity.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        return when {
            !hasPermissions -> {
                activity.requestPermissions(permissions, Configs.REQUEST_CODE_PERMISSION)
                false
            }

            else -> true
        }
    }

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
    fun encodeImage(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val previewWidth = 512
        val previewHeight = bitmap.height * previewWidth / bitmap.height
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)

        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    /**
     * 計算兩點距離
     */
    fun calculateDistance(
        start: LatLng,
        end: LatLng
    ): Double {
        val earthRadius = 6371
        val startLat = (Math.PI / 180) * start.latitude
        val endLat = (Math.PI / 180) * end.latitude

        val startLng = (Math.PI / 180) * start.longitude
        val endLng = (Math.PI / 180) * end.longitude

        // 計算結果原先是以公里為單位，乘以 1000 後即為公尺
        return acos(sin(startLat) * sin(endLat) + cos(startLat) * cos(endLat) * cos(startLng - endLng)) * earthRadius * 1000
    }
}