package mai.project.foodmap.spike.crypto

/**
 * 以 javax.crypto 參考實作（與 App 的 `data/utils/AES.kt` 相同演算法）產生的標準答案。
 *
 * 使用測試專用金鑰，與正式環境的 AES_KEY 無關。`jvmTest` 會再用參考實作重新驗證這些值，
 * 所以這份檔案本身也受到檢查；iOS 只要通過同一組答案，就代表與現有 App 相容。
 *
 * 涵蓋：空字串、剛好 15 / 16 / 17 位元組（區塊邊界）、中文、emoji（4 位元組 UTF-8）、
 * 多區塊長字串、非 ASCII 金鑰、空金鑰。
 */
internal object GoldenVectors {

    data class Vector(val key: String, val plaintext: String, val ciphertext: String)

    private const val KEY = "food-map-compat-test-key"

    val all: List<Vector> = listOf(
        Vector(key = KEY, plaintext = "", ciphertext = "0RQ6Sn+m0pz4RkIZLhlDVA=="),
        Vector(key = KEY, plaintext = "password123", ciphertext = "WvBw2X5VaiYveiUiiSyMZA=="),
        Vector(key = KEY, plaintext = "abcdefghijklmno", ciphertext = "yiyq/ccksGagDSn5I0+tng=="),
        Vector(key = KEY, plaintext = "abcdefghijklmnop", ciphertext = "JnfbqUHQWMTPdbK8Uc5ngp5XiIRa79pwnCM+y7NJkUM="),
        Vector(key = KEY, plaintext = "abcdefghijklmnopq", ciphertext = "JnfbqUHQWMTPdbK8Uc5ngrcFBJem6y1jolWcMrhF9j8="),
        Vector(key = KEY, plaintext = "美食地圖密碼", ciphertext = "uhIo1jgayI7bfRbC+47x08sFwP6e+g2Y4WTE/DAgdR4="),
        Vector(key = KEY, plaintext = "P@ss w0rd!🍜", ciphertext = "jrEfaWrPgQNPLMuUu5q/Kw=="),
        Vector(
            key = KEY,
            plaintext = "The quick brown fox jumps over the lazy dog. 敏捷的棕色狐狸跳過了懶狗。",
            ciphertext = "KSp8EE986gyPr3kvlIV/MkCj+YItJJ4oAA0NI/rAu1w1zBlVIJuPW5E7EETsmDpXSnEDwZTRMVT0cuZmCV8zFnAFmVY2jeTioh7E9pw6S9cvjM49VNVxzBHKsxE2EE20",
        ),
        Vector(key = "測試金鑰-🔑", plaintext = "password123", ciphertext = "F6KfHgEAvZ0TxyePiFBoLw=="),
        Vector(key = "", plaintext = "x", ciphertext = "c642srIu1tZ0rl4TXl+gVg=="),
    )
}
