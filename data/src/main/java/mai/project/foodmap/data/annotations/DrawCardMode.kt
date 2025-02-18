package mai.project.foodmap.data.annotations

import androidx.annotation.IntDef

@IntDef(
    DrawCardMode.NEAREST,
    DrawCardMode.FAVORITE
)
@Retention(AnnotationRetention.SOURCE)
/**
 * 抽取卡片模式
 */
annotation class DrawCardMode {
    companion object {
        /**
         * 附近的人氣餐廳
         */
        const val NEAREST = 0

        /**
         * 最愛清單的人氣餐廳
         */
        const val FAVORITE = 1
    }
}
