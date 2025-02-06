package mai.project.foodmap.data.repositoryImpl

import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.flow.firstOrNull
import mai.project.foodmap.data.mapper.mapToEmptyNetworkResult
import mai.project.foodmap.data.remoteDataSource.APIService
import mai.project.foodmap.data.remoteDataSource.models.user.LoginReq
import mai.project.foodmap.data.remoteDataSource.models.user.LogoutReq
import mai.project.foodmap.data.remoteDataSource.models.user.RegisterReq
import mai.project.foodmap.data.utils.AES
import mai.project.foodmap.data.utils.handleAPIResponse
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class UserRepoImpl @Inject constructor(
    private val apiService: APIService,
    private val preferenceRepo: PreferenceRepo,
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
            preferenceRepo.writeUsername(if (isRemember) username else "")
            preferenceRepo.writePassword(if (isRemember) password else "")
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
            preferenceRepo.writeUsername(if (isRemember) username else "")
            preferenceRepo.writePassword(if (isRemember) password else "")
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

        if (result is NetworkResult.Success) {
            // 清空系統資料
            preferenceRepo.clearAll()
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
}