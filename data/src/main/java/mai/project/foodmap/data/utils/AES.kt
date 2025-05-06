package mai.project.foodmap.data.utils

import android.util.Base64
import mai.project.foodmap.data.BuildConfig
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES 加密演算法
 *
 * @property encrypt 加密方法
 * @property decrypt 解密方法
 */
internal object AES {
    private const val KEY_ALIAS = BuildConfig.AES_KEY
    private const val AES_MODE = "AES/CBC/PKCS7Padding"
    private const val CHARSET = "UTF-8"
    private const val CIPHER = "AES"
    private const val HASH_ALGORITHM = "SHA-256"
    private val IV_BYTES = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

    /**
     * 加密方法
     *
     * 使用 256 位元 AES Key 以及從 SecretKey 產生的金鑰對訊息進行加密和編碼。
     *
     * @param secretKey 用於產生金鑰
     * @param message  你想要加密的東西假設字串是UTF-8
     * @throws GeneralSecurityException 如果加密出現問題
     */
    fun encrypt(secretKey: String = KEY_ALIAS, message: String): String? {
        return try {
            val key = generateKey(secretKey)
            val cipherText = encrypt(key, IV_BYTES, message.toByteArray(charset(CHARSET)))
            // NO_WRAP is important as was getting \n at the end
            return Base64.encodeToString(cipherText, Base64.NO_WRAP)
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: GeneralSecurityException) {
            null
        }
    }

    /**
     * @param key AES key 通常為 128, 192 or 256 bit
     * @param iv 起始向量 (Initiation Vector)
     * @param message 以 bytes 為單位（假設它已經被解碼）
     * @throws GeneralSecurityException 如果加密出現問題
     */
    fun encrypt(key: SecretKeySpec, iv: ByteArray, message: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(AES_MODE)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            return cipher.doFinal(message)
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: GeneralSecurityException) {
            null
        }
    }

    /**
     * 產生用作金鑰的 SecretKey 的 SHA256 雜湊值
     *
     * @param secretKey 用於產生金鑰
     * @return SecretKey 的 SHA256
     */
    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    private fun generateKey(secretKey: String): SecretKeySpec {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val bytes = secretKey.toByteArray(charset(CHARSET))
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()

        return SecretKeySpec(key, CIPHER)
    }

    /**
     * 解密方法
     *
     * 使用 256 位元 AES Key 以及從 SecretKey 產生的金鑰來解密和解碼密文
     *
     * @param secretKey 用於產生金鑰
     * @param base64EncodedCipherText 使用 Base64 編碼的加密訊息
     * @throws GeneralSecurityException 如果解密出現問題
     */
    fun decrypt(secretKey: String = KEY_ALIAS, base64EncodedCipherText: String): String? {
        return try {
            val key = generateKey(secretKey)
            val decodedCipherText = Base64.decode(base64EncodedCipherText, Base64.NO_WRAP)
            val decryptedBytes = decrypt(key, IV_BYTES, decodedCipherText)
            decryptedBytes?.let { String(it, charset(CHARSET)) }
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: GeneralSecurityException) {
            null
        }
    }

    /**
     * @param key AES key 通常為 128, 192 or 256 bit
     * @param iv 起始向量 (Initiation Vector)
     * @param decodedCipherText 以 bytes 為單位（假設它已經被解碼）
     * @throws GeneralSecurityException 如果加密過程中出現問題
     */
    fun decrypt(key: SecretKeySpec, iv: ByteArray, decodedCipherText: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(AES_MODE)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            cipher.doFinal(decodedCipherText)
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: GeneralSecurityException) {
            null
        }
    }
}