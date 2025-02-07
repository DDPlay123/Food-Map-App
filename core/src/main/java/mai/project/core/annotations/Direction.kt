package mai.project.core.annotations

import androidx.annotation.IntDef

@IntDef(Direction.HORIZONTAL, Direction.VERTICAL)
@Retention(AnnotationRetention.SOURCE)
/**
 * 表示方向
 */
annotation class Direction {
    companion object {
        /**
         * 水平
         */
        const val HORIZONTAL = 0

        /**
         * 垂直
         */
        const val VERTICAL = 1
    }
}