package mai.project.foodmap.domain.repository

import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Singleton

@Singleton
interface UserRepo {

    /**
     * 登入
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

    suspend fun logout(): NetworkResult<EmptyNetworkResult>
}