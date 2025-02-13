package mai.project.foodmap.data.repositoryImpl

import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import mai.project.foodmap.data.localDataSource.dao.MyBlacklistDao
import mai.project.foodmap.data.localDataSource.dao.MyFavoriteDao
import mai.project.foodmap.data.localDataSource.dao.MySavedPlaceDao
import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity
import mai.project.foodmap.data.mapper.mapToEmptyNetworkResult
import mai.project.foodmap.data.mapper.mapToMyBlacklistEntities
import mai.project.foodmap.data.mapper.mapToMyFavoriteEntities
import mai.project.foodmap.data.mapper.mapToMyFavoriteResult
import mai.project.foodmap.data.mapper.mapToMyPlaceResults
import mai.project.foodmap.data.mapper.mapToMySavedPlaceEntities
import mai.project.foodmap.data.mapper.mapToRestaurantResultsWithGetBlacklistRes
import mai.project.foodmap.data.remoteDataSource.APIService
import mai.project.foodmap.data.remoteDataSource.models.AddFcmTokenReq
import mai.project.foodmap.data.remoteDataSource.models.DeleteAccountReq
import mai.project.foodmap.data.remoteDataSource.models.GetBlacklistReq
import mai.project.foodmap.data.remoteDataSource.models.GetFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.GetUserImageReq
import mai.project.foodmap.data.remoteDataSource.models.LocationModel
import mai.project.foodmap.data.remoteDataSource.models.LoginReq
import mai.project.foodmap.data.remoteDataSource.models.LogoutReq
import mai.project.foodmap.data.remoteDataSource.models.PullBlacklistReq
import mai.project.foodmap.data.remoteDataSource.models.PullFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.PullPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.PushBlacklistReq
import mai.project.foodmap.data.remoteDataSource.models.PushFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.PushPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.RegisterReq
import mai.project.foodmap.data.remoteDataSource.models.SetPasswordReq
import mai.project.foodmap.data.remoteDataSource.models.SetUserImageReq
import mai.project.foodmap.data.utils.AES
import mai.project.foodmap.data.utils.handleAPIResponse
import mai.project.foodmap.data.utils.safeIoWorker
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.domain.models.MyPlaceResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class UserRepoImpl @Inject constructor(
    private val apiService: APIService,
    private val preferenceRepo: PreferenceRepo,
    private val mySavedPlaceDao: MySavedPlaceDao,
    private val myFavoriteDao: MyFavoriteDao,
    private val myBlacklistDao: MyBlacklistDao
) : UserRepo {

    override suspend fun login(
        username: String,
        password: String,
        isRemember: Boolean,
    ): NetworkResult<EmptyNetworkResult> {
        val deviceId = fetchFid()

        if (deviceId.isEmpty()) {
            return NetworkResult.Error(message = "deviceId is null")
        }

        val result = handleAPIResponse(
            apiService.login(
                LoginReq(
                    username = username,
                    password = AES.encrypt(message = password) ?: password,
                    deviceId = deviceId
                )
            )
        )

        result.data?.result?.run {
            preferenceRepo.writeAccount(if (isRemember) username else "")
            preferenceRepo.writePassword(if (isRemember) password else "")
            preferenceRepo.writeUsername(username)
            preferenceRepo.writeAccessKey(accessKey)
            preferenceRepo.writeUserId(userId)
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun register(
        username: String,
        password: String,
        isRemember: Boolean,
    ): NetworkResult<EmptyNetworkResult> {
        val deviceId = fetchFid()

        if (deviceId.isEmpty()) {
            return NetworkResult.Error(message = "deviceId is null")
        }

        val result = handleAPIResponse(
            apiService.register(
                RegisterReq(
                    username = username,
                    password = AES.encrypt(message = password) ?: password,
                    deviceId = deviceId
                )
            )
        )

        result.data?.result?.run {
            preferenceRepo.writeAccount(if (isRemember) username else "")
            preferenceRepo.writePassword(if (isRemember) password else "")
            preferenceRepo.writeUsername(username)
            preferenceRepo.writeAccessKey(accessKey)
            preferenceRepo.writeUserId(userId)
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun logout(): NetworkResult<EmptyNetworkResult> {
        val deviceId = fetchFid()

        if (deviceId.isEmpty()) {
            return NetworkResult.Error(message = "deviceId is null")
        }

        val result = handleAPIResponse(
            apiService.logout(
                LogoutReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    deviceId = deviceId
                )
            )
        )

        // 清空系統資料
        preferenceRepo.clearAll()

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun deleteAccount(): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.deleteAccount(
                DeleteAccountReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty()
                )
            )
        )

        if (result is NetworkResult.Success) {
            // 清空系統資料 (含已儲存的帳密)
            preferenceRepo.writeAccount("")
            preferenceRepo.writePassword("")
            preferenceRepo.clearAll()
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun addFcmToken(): NetworkResult<EmptyNetworkResult> {
        val deviceId = fetchFid()
        val fcmToken = fetchFcmToken()

        if (deviceId.isEmpty()) {
            return NetworkResult.Error(message = "deviceId is null")
        }

        return handleAPIResponse(
            apiService.addFcmToken(
                AddFcmTokenReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    deviceId = deviceId,
                    fcmToken = fcmToken
                )
            )
        ).mapToEmptyNetworkResult()
    }

    override suspend fun setPassword(password: String): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.setPassword(
                SetPasswordReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    password = AES.encrypt(message = password) ?: password
                )
            )
        )

        // 需要覆寫密碼
        if (result is NetworkResult.Success && preferenceRepo.readPassword.isNotEmpty()) {
            preferenceRepo.writePassword(password)
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun setUserImage(userImage: String): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.setUserImage(
                SetUserImageReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    userImage = userImage
                )
            )
        )

        if (result is NetworkResult.Success) {
            preferenceRepo.writeUserImage(userImage)
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun getUserImage(): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.getUserImage(
                GetUserImageReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty()
                )
            )
        )

        result.data?.result?.userImage?.let {
            preferenceRepo.writeUserImage(it)
        }

        return result.mapToEmptyNetworkResult()
    }

    override val getMyPlaceList: Flow<List<MyPlaceResult>>
        get() = mySavedPlaceDao.readMySavedPlaceList().map {
            it.map { entity -> entity.result }
        }

    override suspend fun fetchMyPlaceList(): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.getPlaceList(
                GetPlaceListReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty()
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            mySavedPlaceDao.syncMySavedPlaceList(
                result.mapToMyPlaceResults().mapToMySavedPlaceEntities()
            )
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun pushMyPlace(
        placeId: String,
        name: String,
        address: String,
        lat: Double,
        lng: Double
    ): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.pushPlaceList(
                PushPlaceListReq(
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    placeId = placeId,
                    name = name,
                    address = address,
                    location = LocationModel(lat = lat, lng = lng)
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            mySavedPlaceDao.insertMySavedPlace(
                MySavedPlaceEntity(
                    index = placeId,
                    result = MyPlaceResult(
                        placeCount = getMyPlaceList.firstOrNull()?.size ?: -1,
                        placeId = placeId,
                        name = name,
                        address = address,
                        lat = lat,
                        lng = lng
                    ),
                )
            )
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun pullMyPlace(placeId: String): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.pullPlaceList(
                PullPlaceListReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    placeId = placeId
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            mySavedPlaceDao.deleteMySavedPlace(placeId)
        }

        return result.mapToEmptyNetworkResult()
    }

    override val getMyFavoriteList: Flow<List<MyFavoriteResult>>
        get() = myFavoriteDao.readMyFavoriteList().map {
            it.map { entity -> entity.result }
        }

    override suspend fun fetchMyFavoriteList(): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.getFavoriteList(
                GetFavoriteListReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty()
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            val entities = result.mapToMyFavoriteResult(preferenceRepo.readUserId.firstOrNull().orEmpty())
                .mapToMyFavoriteEntities()
            preferenceRepo.writeMyFavoritePlaceIds(entities.map { it.result.placeId }.toSet())
            myFavoriteDao.syncMyFavoriteList(entities)
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun pushOrPullMyFavorite(
        placeId: String,
        isFavorite: Boolean
    ): NetworkResult<EmptyNetworkResult> {
        return if (isFavorite) pushMyFavorite(placeId) else pullMyFavorite(placeId)
    }

    override val getMyBlacklist: Flow<List<RestaurantResult>>
        get() = myBlacklistDao.readMyBlackList().map {
            it.map { entity -> entity.result }
        }

    override suspend fun fetchMyBlacklist(): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.getBlacklist(
                GetBlacklistReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty()
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            val entities = result.mapToRestaurantResultsWithGetBlacklistRes(preferenceRepo.readUserId.firstOrNull().orEmpty())
                .mapToMyBlacklistEntities()
            preferenceRepo.writeMyBlacklistPlaceIds(entities.map { it.result.placeId }.toSet())
            myBlacklistDao.syncMyBlacklist(entities)
        }

        return result.mapToEmptyNetworkResult()
    }

    override suspend fun pushOrPullMyBlocked(placeId: String, isBlocked: Boolean): NetworkResult<EmptyNetworkResult> {
        return if (isBlocked) pushMyBlocked(placeId) else pullMyBlocked(placeId)
    }

    private suspend fun pushMyFavorite(placeId: String): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.pushFavoriteList(
                PushFavoriteListReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    favoriteList = listOf(placeId)
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            preferenceRepo.addMyFavoritePlaceId(placeId)
        }

        return result.mapToEmptyNetworkResult()
    }

    private suspend fun pullMyFavorite(placeId: String): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.pullFavoriteList(
                PullFavoriteListReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    favoriteIdList = listOf(placeId)
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            preferenceRepo.removeMyFavoritePlaceId(placeId)
            myFavoriteDao.deleteMyFavorite(placeId)
        }

        return result.mapToEmptyNetworkResult()
    }

    private suspend fun pushMyBlocked(placeId: String): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.pushBlacklist(
                PushBlacklistReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    placeIdList = listOf(placeId)
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            preferenceRepo.addMyBlockedPlaceId(placeId)
        }

        return result.mapToEmptyNetworkResult()
    }

    private suspend fun pullMyBlocked(placeId: String): NetworkResult<EmptyNetworkResult> {
        val result = handleAPIResponse(
            apiService.pullBlacklist(
                PullBlacklistReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    placeIdList = listOf(placeId)
                )
            )
        )

        if (result is NetworkResult.Success) safeIoWorker {
            preferenceRepo.removeMyBlockedPlaceId(placeId)
            myBlacklistDao.deleteMyBlocked(placeId)
        }

        return result.mapToEmptyNetworkResult()
    }

    /**
     * 取得當前的 FID
     */
    private suspend fun fetchFid(): String {
        val savedFid = preferenceRepo.readFid.firstOrNull()
        if (!savedFid.isNullOrEmpty()) return savedFid

        return suspendCoroutine { continuation ->
            FirebaseInstallations.getInstance().id
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result.orEmpty())
                    } else {
                        continuation.resume("")
                    }
                }
        }.also { newFid -> if (newFid.isNotEmpty()) preferenceRepo.writeFid(newFid) }
    }

    /**
     * 取得當前的 FCM Token
     */
    private suspend fun fetchFcmToken(): String {
        return suspendCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result.orEmpty())
                    } else {
                        continuation.resume("")
                    }
                }
        }
    }
}