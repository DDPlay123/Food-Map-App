package mai.project.foodmap.domain.repository

import mai.project.foodmap.domain.models.NetworkResult
import javax.inject.Singleton

@Singleton
interface UserRepo {

    /**
     * 登入
     */
    suspend fun login(
        username: String,
        password: String,
        deviceId: String
    ): NetworkResult<Nothing>

    /**
     * 註冊帳號
     */
    suspend fun register(
        username: String,
        password: String,
        deviceId: String
    ): NetworkResult<Nothing>
}