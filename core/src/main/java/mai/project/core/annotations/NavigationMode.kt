package mai.project.core.annotations

import androidx.annotation.StringDef

@StringDef(
    NavigationMode.CAR,
    NavigationMode.BICYCLE,
    NavigationMode.MOTORCYCLE,
    NavigationMode.WALKING
)
@Retention(AnnotationRetention.SOURCE)
/**
 * Google 地圖導航模式
 */
annotation class NavigationMode {
    companion object {
        /**
         * 汽車
         */
        const val CAR = "d"

        /**
         * 腳踏車
         */
        const val BICYCLE = "b"

        /**
         * 機車
         */
        const val MOTORCYCLE = "l"

        /**
         * 走路
         */
        const val WALKING = "w"
    }
}
