package mai.project.foodmap.spike.crypto

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.operations.IvCipher
import kotlin.io.encoding.Base64

/**
 * 與現有 Android App（`data/utils/AES.kt`）逐位元組相容的加解密實作。
 *
 * 後端無法改動，所以演算法必須和既有協定完全一致：
 * - 金鑰：`secretKey` 的 UTF-8 位元組做 SHA-256，得到 256 位元 AES 金鑰
 * - 模式：AES/CBC，PKCS#7 padding
 * - IV：固定 16 個 0x00
 * - 輸出：標準 Base64、有 padding、不換行（對應 `android.util.Base64.NO_WRAP`）
 *
 * 固定 IV 會讓相同明文產生相同密文。這是既有協定的一部分，不是這裡的設計選擇。
 *
 * 和原本的實作一樣，任何失敗（格式錯誤、padding 錯誤）都回傳 `null`，不拋例外。
 */
@OptIn(DelicateCryptographyApi::class)
class LegacyPasswordCipher(
    secretKey: String,
    provider: CryptographyProvider = platformCryptographyProvider,
) {
    private val cipher: IvCipher = run {
        val keyBytes = provider.get(SHA256).hasher().hashBlocking(secretKey.encodeToByteArray())
        provider.get(AES.CBC)
            .keyDecoder()
            .decodeFromByteArrayBlocking(AES.Key.Format.RAW, keyBytes)
            .cipher(padding = true)
    }

    /** 加密並以 Base64 編碼；失敗時回傳 `null`。 */
    fun encrypt(message: String): String? = runCatching {
        Base64.encode(cipher.encryptWithIvBlocking(ZERO_IV, message.encodeToByteArray()))
    }.getOrNull()

    /** 解碼 Base64 並解密；失敗時回傳 `null`。 */
    fun decrypt(base64CipherText: String): String? = runCatching {
        cipher.decryptWithIvBlocking(ZERO_IV, Base64.decode(base64CipherText)).decodeToString()
    }.getOrNull()

    private companion object {
        val ZERO_IV = ByteArray(16)
    }
}
