package mai.project.foodmap.data.remoteDataSource

import mai.project.foodmap.data.remoteDataSource.models.user.AddFcmTokenReq
import mai.project.foodmap.data.remoteDataSource.models.user.AddFcmTokenRes
import mai.project.foodmap.data.remoteDataSource.models.user.DeleteAccountReq
import mai.project.foodmap.data.remoteDataSource.models.user.DeleteAccountRes
import mai.project.foodmap.data.remoteDataSource.models.user.GetUserImageReq
import mai.project.foodmap.data.remoteDataSource.models.user.GetUserImageRes
import mai.project.foodmap.data.remoteDataSource.models.user.LoginReq
import mai.project.foodmap.data.remoteDataSource.models.user.LoginRes
import mai.project.foodmap.data.remoteDataSource.models.user.LogoutReq
import mai.project.foodmap.data.remoteDataSource.models.user.LogoutRes
import mai.project.foodmap.data.remoteDataSource.models.user.RegisterReq
import mai.project.foodmap.data.remoteDataSource.models.user.RegisterRes
import mai.project.foodmap.data.remoteDataSource.models.user.SetPasswordReq
import mai.project.foodmap.data.remoteDataSource.models.user.SetPasswordRes
import mai.project.foodmap.data.remoteDataSource.models.user.SetUserImageReq
import mai.project.foodmap.data.remoteDataSource.models.user.SetUserImageRes
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

    @Headers("Content-Type: application/json")
    @POST("api/user/delete_account")
    suspend fun deleteAccount(
        @Body body: DeleteAccountReq
    ): Response<DeleteAccountRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/add_fcm_token")
    suspend fun addFcmToken(
        @Body body: AddFcmTokenReq
    ): Response<AddFcmTokenRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/set_password")
    suspend fun setPassword(
        @Body body: SetPasswordReq
    ): Response<SetPasswordRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/set_image")
    suspend fun setUserImage(
        @Body body: SetUserImageReq
    ): Response<SetUserImageRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/get_image")
    suspend fun getUserImage(
        @Body body: GetUserImageReq
    ): Response<GetUserImageRes>
    // endregion user
}