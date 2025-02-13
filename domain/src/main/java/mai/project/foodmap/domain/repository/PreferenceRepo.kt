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
     * 寫入/讀取 使用者帳號
     */
    fun writeAccount(account: String)
    val readAccount: String

    /**
     * 寫入/讀取 使用者密碼
     */
    fun writePassword(password: String)
    val readPassword: String

    /**
     * 寫入/讀取 大頭貼
     */
    suspend fun writeUserImage(userImage: String)
    val readUserImage: Flow<String>

    /**
     * 寫入/讀取 顯示模式
     */
    suspend fun writeThemeMode(theme: Int)
    val readThemeMode: Flow<Int>

    /**
     * 寫入/讀取 語言模式
     */
    suspend fun writeLanguageMode(language: String)
    val readLanguageMode: Flow<String>

    /**
     * 讀取/寫入 定位點 (placeId，如果為空，表示當前位置)
     */
    suspend fun writeMyPlaceId(placeId: String)
    val readMyPlaceId: Flow<String>

    /**
     * 寫入/讀取 我的收藏清單 PlaceId
     */
    suspend fun writeMyFavoritePlaceIds(placeIds: Set<String>)
    suspend fun addMyFavoritePlaceId(placeId: String)
    suspend fun removeMyFavoritePlaceId(placeId: String)
    val readMyFavoritePlaceIds: Flow<Set<String>>

    /**
     * 寫入/讀取 我的黑名單 PlaceId
     */
    suspend fun writeMyBlockPlaceIds(placeIds: Set<String>)
    suspend fun addMyBlockPlaceId(placeId: String)
    suspend fun removeMyBlockPlaceId(placeId: String)
    val readMyBlockPlaceIds: Flow<Set<String>>
}