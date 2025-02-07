package mai.project.core.annotations

import androidx.annotation.IntDef

@IntDef(
    ImageType.NONE,
    ImageType.DEFAULT,
    ImageType.PERSON
)
@Retention(AnnotationRetention.SOURCE)
/**
 * 圖片類型 (用於本地端顯示圖片的類型)
 */
annotation class ImageType {
    companion object {
        /**
         * 無
         */
        const val NONE = -1

        /**
         * 預設
         */
        const val DEFAULT = 0

        /**
         * 人物
         */
        const val PERSON = 1
    }
}
