package mai.project.foodmap.data.annotations

import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatDelegate

@IntDef(
    ThemeMode.SYSTEM,
    ThemeMode.LIGHT,
    ThemeMode.DARK
)
@Retention(AnnotationRetention.SOURCE)
/**
 * 顯示模式
 */
annotation class ThemeMode {
    companion object {
        /**
         * 系統預設
         */
        const val SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        /**
         * 淺色
         */
        const val LIGHT = AppCompatDelegate.MODE_NIGHT_NO

        /**
         * 深色
         */
        const val DARK = AppCompatDelegate.MODE_NIGHT_YES
    }
}
