package mai.project.foodmap.spike.crypto

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 只在 JVM 執行：拿跨平台實作和 javax.crypto 參考實作逐一比對。
 */
class ReferenceCrossCheckTest {

    @Test
    fun goldenVectorsMatchJavaxCryptoReference() {
        // 確保 commonTest 的標準答案本身是正確的
        GoldenVectors.all.forEach { v ->
            assertEquals(v.ciphertext, LegacyAesReference.encrypt(v.key, v.plaintext), "plaintext=\"${v.plaintext}\"")
        }
    }

    @Test
    fun randomInputsMatchReference() {
        val random = Random(20261123)
        repeat(ITERATIONS) { i ->
            val key = random.nextText(maxCodePoints = 24)
            val plaintext = random.nextText(maxCodePoints = 80)
            assertSameAsReference(key, plaintext, "iteration=$i")
        }
    }

    @Test
    fun realKeyMatchesReferenceWhenAvailable() {
        // 若環境變數有正式金鑰，也用它比對一次。金鑰不會被輸出到任何地方。
        // local.properties 的寫法是 AES_KEY = "..."，BuildConfig 取的是引號內的值。
        val realKey = System.getenv("AES_KEY")?.trim()?.removeSurrounding("\"") ?: return
        val random = Random(42)
        repeat(200) { i ->
            assertSameAsReference(realKey, random.nextText(maxCodePoints = 40), "real-key iteration=$i")
        }
    }

    private fun assertSameAsReference(key: String, plaintext: String, label: String) {
        val expected = LegacyAesReference.encrypt(key, plaintext)
        val cipher = LegacyPasswordCipher(key)
        assertEquals(expected, cipher.encrypt(plaintext), "encrypt mismatch ($label)")
        assertEquals(plaintext, cipher.decrypt(expected), "decrypt mismatch ($label)")
        assertEquals(plaintext, LegacyAesReference.decrypt(key, cipher.encrypt(plaintext)!!), "reverse decrypt mismatch ($label)")
    }

    /**
     * 產生混合 ASCII、拉丁補充、中文、日文假名與 emoji 的字串。
     * 刻意不產生落單的 surrogate：鍵盤輸入不會出現，且各平台對它的 UTF-8 替代字元本來就不同。
     */
    private fun Random.nextText(maxCodePoints: Int): String {
        val sb = StringBuilder()
        repeat(nextInt(0, maxCodePoints + 1)) {
            val codePoint = when (nextInt(5)) {
                0, 1 -> nextInt(0x20, 0x7F)
                2 -> nextInt(0xA0, 0x100)
                3 -> if (nextBoolean()) nextInt(0x4E00, 0xA000) else nextInt(0x3041, 0x3097)
                else -> nextInt(0x1F300, 0x1FB00)
            }
            sb.appendCodePoint(codePoint)
        }
        return sb.toString()
    }

    private companion object {
        const val ITERATIONS = 2000
    }
}
