package mai.project.foodmap.data.repositoryImpl

import mai.project.foodmap.data.remoteDataSource.APIService
import mai.project.foodmap.data.remoteDataSource.models.user.LoginReq
import mai.project.foodmap.data.remoteDataSource.models.user.RegisterReq
import mai.project.foodmap.data.utils.handleAPIResponse
import mai.project.foodmap.data.utils.mapToNothing
import mai.project.foodmap.domain.models.NetworkResult
import mai.project.foodmap.domain.repository.UserRepo
import javax.inject.Inject

internal class UserRepoImpl @Inject constructor(
    private val apiService: APIService
) : UserRepo {

    override suspend fun login(
        username: String,
        password: String,
        deviceId: String
    ): NetworkResult<Nothing> {
        val result = handleAPIResponse(
            apiService.login(
                LoginReq(
                    username = username,
                    password = password,
                    deviceId = deviceId
                )
            )
        )
        if (result is NetworkResult.Success) {
            // TODO 儲存資料
        }
        return result.mapToNothing()
    }

    override suspend fun register(
        username: String,
        password: String,
        deviceId: String
    ): NetworkResult<Nothing> {
        val result = handleAPIResponse(
            apiService.register(
                RegisterReq(
                    username = username,
                    password = password,
                    deviceId = deviceId
                )
            )
        )
        if (result is NetworkResult.Success) {
            // TODO 儲存資料
        }
        return result.mapToNothing()
    }
}