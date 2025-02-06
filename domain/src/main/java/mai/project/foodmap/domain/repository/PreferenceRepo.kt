package mai.project.foodmap.domain.repository

import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
interface PreferenceRepo {

    /**
     * 清空所有資料
     */
    suspend fun clearAll()

    /**
     * 寫入/讀取 FID
     */
    suspend fun writeFid(fid: String)
    val readFid: Flow<String>

    /**
     * 寫入/讀取 UserId
     */
    suspend fun writeUserId(userId: String)
    val readUserId: Flow<String>

    /**
     * 寫入/讀取 AccessKey
     */
    suspend fun writeAccessKey(accessKey: String)
    val readAccessKey: Flow<String>

    /**
     * 寫入/讀取 使用者名稱
     */
    suspend fun writeUsername(username: String)
    val readUsername: Flow<String>

    /**
     * 寫入/讀取 使用者密碼
     */
    suspend fun writePassword(password: String)
    val readPassword: Flow<String>

    /**
     * 寫入/讀取 FCM Token
     */
    suspend fun writeFcmToken(fcmToken: String)
    val readFcmToken: Flow<String>
}