package mai.project.foodmap.domain.repository

import kotlinx.coroutines.flow.Flow
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.MyBlacklistResult
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.domain.models.MyPlaceResult
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

    /**
     * 讀取儲存的定位點資料
     */
    val getMyPlaceList: Flow<List<MyPlaceResult>>

    /**
     * 抓取儲存的定位點
     */
    suspend fun fetchMyPlaceList(): NetworkResult<EmptyNetworkResult>

    /**
     * 儲存定位點
     */
    suspend fun pushMyPlace(
        placeId: String,
        name: String,
        address: String,
        lat: Double,
        lng: Double
    ): NetworkResult<EmptyNetworkResult>

    /**
     * 移除定位點
     */
    suspend fun pullMyPlace(
        placeId: String
    ): NetworkResult<EmptyNetworkResult>

    /**
     * 讀取我的收藏清單
     */
    val getMyFavoriteList: Flow<List<MyFavoriteResult>>

    /**
     * 抓取收藏清單
     */
    suspend fun fetchMyFavoriteList(): NetworkResult<EmptyNetworkResult>

    /**
     * 新增/移除 收藏
     */
    suspend fun pushOrPullMyFavorite(
        placeId: String,
        isFavorite: Boolean
    ): NetworkResult<EmptyNetworkResult>

    /**
     * 讀取我的黑名單列表
     */
    val getMyBlacklist: Flow<List<MyBlacklistResult>>

    /**
     * 抓取黑名單
     */
    suspend fun fetchMyBlacklist(): NetworkResult<EmptyNetworkResult>

    /**
     * 新增/移除 黑名單
     */
    suspend fun pushOrPullMyBlocked(
        placeId: String,
        isBlocked: Boolean
    ): NetworkResult<EmptyNetworkResult>
}