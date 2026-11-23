package mai.project.foodmap.spike.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 在每個平台（JVM、iOS）上執行，確認輸出與現有 App 的標準答案一致。
 */
class LegacyPasswordCipherTest {

    @Test
    fun encryptMatchesGoldenVectors() {
        GoldenVectors.all.forEach { v ->
            assertEquals(
                expected = v.ciphertext,
                actual = LegacyPasswordCipher(v.key).encrypt(v.plaintext),
                message = "key=\"${v.key}\" plaintext=\"${v.plaintext}\"",
            )
        }
    }

    @Test
    fun decryptRecoversGoldenPlaintext() {
        GoldenVectors.all.forEach { v ->
            assertEquals(
                expected = v.plaintext,
                actual = LegacyPasswordCipher(v.key).decrypt(v.ciphertext),
                message = "key=\"${v.key}\" ciphertext=\"${v.ciphertext}\"",
            )
        }
    }

    @Test
    fun sameInputAlwaysProducesSameOutput() {
        // 固定 IV 是既有協定的一部分：同一組帳密每次送出的密文都相同
        val cipher = LegacyPasswordCipher("food-map-compat-test-key")
        assertEquals(cipher.encrypt("password123"), cipher.encrypt("password123"))
    }

    @Test
    fun decryptReturnsNullForInvalidInput() {
        val cipher = LegacyPasswordCipher("food-map-compat-test-key")
        // 不是合法的 Base64
        assertNull(cipher.decrypt("not base64!"))
        // 合法 Base64，但長度不是 AES 區塊的倍數
        assertNull(cipher.decrypt("AAAA"))
        // 用錯誤金鑰解密，padding 驗證失敗
        assertNull(LegacyPasswordCipher("wrong-key").decrypt("WvBw2X5VaiYveiUiiSyMZA=="))
    }
}
