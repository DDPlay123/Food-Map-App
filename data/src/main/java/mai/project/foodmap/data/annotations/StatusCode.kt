package mai.project.foodmap.data.annotations

import androidx.annotation.IntDef

@IntDef(
    StatusCode.UNKNOWN,
    StatusCode.SUCCESS,
    StatusCode.PASSWORD_FORMAT_ERROR,
    StatusCode.ALREADY_REGISTER,
    StatusCode.ACCOUNT_NOT_EXIST,
    StatusCode.ACCESS_KEY_ERROR
)
@Retention(AnnotationRetention.SOURCE)
/**
 * API Response Status code (狀態碼)
 */
internal annotation class StatusCode {
    companion object {
        /**
         * 未知錯誤
         */
        const val UNKNOWN = -1

        /**
         * 成功響應
         */
        const val SUCCESS = 0

        /**
         * 帳號密碼格式錯誤
         */
        const val PASSWORD_FORMAT_ERROR = 1

        /**
         * 帳號已註冊
         */
        const val ALREADY_REGISTER = 2

        /**
         * 帳號不存在
         */
        const val ACCOUNT_NOT_EXIST = 3

        /**
         * accessKey錯誤
         */
        const val ACCESS_KEY_ERROR = 4
    }
}