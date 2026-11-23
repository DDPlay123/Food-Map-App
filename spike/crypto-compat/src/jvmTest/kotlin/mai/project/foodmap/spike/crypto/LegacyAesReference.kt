package mai.project.foodmap.spike.crypto

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 現有 App `data/utils/AES.kt` 的 JVM 版複本，作為比對基準。
 *
 * 與原始碼僅有兩處差異，兩者在輸出上完全等價：
 * - `AES/CBC/PKCS7Padding` 在標準 JDK 中的名稱是 `AES/CBC/PKCS5Padding`（對 AES 而言是同一種 padding）
 * - `android.util.Base64.NO_WRAP` 對應 `java.util.Base64.getEncoder()`（標準字母表、有 padding、不換行）
 */
internal object LegacyAesReference {

    private val IV = ByteArray(16)

    private fun key(secretKey: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256").digest(secretKey.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }

    fun encrypt(secretKey: String, message: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key(secretKey), IvParameterSpec(IV))
        return Base64.getEncoder().encodeToString(cipher.doFinal(message.toByteArray(Charsets.UTF_8)))
    }

    fun decrypt(secretKey: String, base64CipherText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key(secretKey), IvParameterSpec(IV))
        return String(cipher.doFinal(Base64.getDecoder().decode(base64CipherText)), Charsets.UTF_8)
    }
}
