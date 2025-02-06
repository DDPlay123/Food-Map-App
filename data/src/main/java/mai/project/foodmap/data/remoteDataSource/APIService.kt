package mai.project.foodmap.data.remoteDataSource

import mai.project.foodmap.data.remoteDataSource.models.user.LoginReq
import mai.project.foodmap.data.remoteDataSource.models.user.LoginRes
import mai.project.foodmap.data.remoteDataSource.models.user.LogoutReq
import mai.project.foodmap.data.remoteDataSource.models.user.LogoutRes
import mai.project.foodmap.data.remoteDataSource.models.user.RegisterReq
import mai.project.foodmap.data.remoteDataSource.models.user.RegisterRes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface APIService {

    // region user
    @Headers("Content-Type: application/json")
    @POST("api/user/login")
    suspend fun login(
        @Body body: LoginReq
    ): Response<LoginRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/register")
    suspend fun register(
        @Body body: RegisterReq
    ): Response<RegisterRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/logout")
    suspend fun logout(
        @Body body: LogoutReq
    ): Response<LogoutRes>
    // endregion user
}