package mai.project.foodmap.domain.repository

import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Singleton

@Singleton
interface UserRepo {

    /**
     * 登入帳號
     *
     * @param username 使用者名稱
     * @param password 密碼
     * @param isRemember 是否記住帳號
     */
    suspend fun login(
        username: String,
        password: String,
        isRemember: Boolean,
    ): NetworkResult<EmptyNetworkResult>

    /**
     * 註冊帳號
     *
     * @param username 使用者名稱
     * @param password 密碼
     * @param isRemember 是否記住帳號
     */
    suspend fun register(
        username: String,
        password: String,
        isRemember: Boolean,
    ): NetworkResult<EmptyNetworkResult>

    /**
     * 登出帳號
     */
    suspend fun logout(): NetworkResult<EmptyNetworkResult>

    /**
     * 刪除帳號
     */
    suspend fun deleteAccount(): NetworkResult<EmptyNetworkResult>

    /**
     * 新增 FCM token 至 Server
     */
    suspend fun addFcmToken(): NetworkResult<EmptyNetworkResult>

    /**
     * 重設密碼
     *
     * @param password 密碼
     */
    suspend fun setPassword(
        password: String
    ): NetworkResult<EmptyNetworkResult>

    /**
     * 設定大頭貼
     *
     * @param userImage 大頭貼
     */
    suspend fun setUserImage(
        userImage: String
    ): NetworkResult<EmptyNetworkResult>

    /**
     * 取得大頭貼
     */
    suspend fun getUserImage(): NetworkResult<EmptyNetworkResult>
}