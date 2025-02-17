package mai.project.core

/**
 * App 的常數設定
 */
object Configs {

    /**
     * 使用者名稱 最小長度
     */
    const val USERNAME_LENGTH_MIN = 4

    /**
     * 使用者名稱 最大長度
     */
    const val USERNAME_LENGTH_MAX = 16

    /**
     * 使用者名稱 格式
     *
     * - 必須包含英文字母 (無分大小寫)
     */
    const val USERNAME_FORMATER = ".*[a-zA-Z].*"

    /**
     * 密碼 最小長度
     */
    const val PASSWORD_LENGTH_MIN = 6

    /**
     * 密碼 最大長度
     */
    const val PASSWORD_LENGTH_MAX = 30

    /**
     * 預設的 緯度
     */
    const val DEFAULT_LATITUDE = 25.032972653092518

    /**
     * 預設的 經度
     */
    const val DEFAULT_LONGITUDE = 121.56537596752182

    /**
     * 搜尋的最大距離 (公里)
     */
    const val MAX_SEARCH_DISTANCE = 30

    /**
     * 搜尋的最小距離 (公里)
     */
    const val MIN_SEARCH_DISTANCE = 1
}