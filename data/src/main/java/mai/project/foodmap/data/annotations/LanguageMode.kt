package mai.project.foodmap.data.annotations

import androidx.annotation.StringDef

@StringDef(
    LanguageMode.SYSTEM,
    LanguageMode.ENGLISH,
    LanguageMode.TRADITIONAL_CHINESE
)
@Retention(AnnotationRetention.SOURCE)
/**
 * 語言模式
 */
annotation class LanguageMode {
    companion object {
        /**
         * 系統預設
         */
        const val SYSTEM = ""

        /**
         * 英文
         */
        const val ENGLISH = "en-US"

        /**
         * 繁體中文
         */
        const val TRADITIONAL_CHINESE = "zh-TW"
    }
}
