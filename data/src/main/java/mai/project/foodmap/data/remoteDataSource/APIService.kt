package mai.project.foodmap.data.remoteDataSource

import mai.project.foodmap.data.remoteDataSource.models.DrawCardReq
import mai.project.foodmap.data.remoteDataSource.models.DrawCardRes
import mai.project.foodmap.data.remoteDataSource.models.AddFcmTokenReq
import mai.project.foodmap.data.remoteDataSource.models.AddFcmTokenRes
import mai.project.foodmap.data.remoteDataSource.models.DeleteAccountReq
import mai.project.foodmap.data.remoteDataSource.models.DeleteAccountRes
import mai.project.foodmap.data.remoteDataSource.models.GetFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.GetFavoriteListRes
import mai.project.foodmap.data.remoteDataSource.models.GetLocationByAddressReq
import mai.project.foodmap.data.remoteDataSource.models.GetLocationByAddressRes
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceListRes
import mai.project.foodmap.data.remoteDataSource.models.GetUserImageReq
import mai.project.foodmap.data.remoteDataSource.models.GetUserImageRes
import mai.project.foodmap.data.remoteDataSource.models.LoginReq
import mai.project.foodmap.data.remoteDataSource.models.LoginRes
import mai.project.foodmap.data.remoteDataSource.models.LogoutReq
import mai.project.foodmap.data.remoteDataSource.models.LogoutRes
import mai.project.foodmap.data.remoteDataSource.models.PlaceAutocompleteReq
import mai.project.foodmap.data.remoteDataSource.models.PlaceAutocompleteRes
import mai.project.foodmap.data.remoteDataSource.models.PullFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.PullFavoriteListRes
import mai.project.foodmap.data.remoteDataSource.models.PullPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.PullPlaceListRes
import mai.project.foodmap.data.remoteDataSource.models.PushFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.PushFavoriteListRes
import mai.project.foodmap.data.remoteDataSource.models.PushPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.PushPlaceListRes
import mai.project.foodmap.data.remoteDataSource.models.RegisterReq
import mai.project.foodmap.data.remoteDataSource.models.RegisterRes
import mai.project.foodmap.data.remoteDataSource.models.SetPasswordReq
import mai.project.foodmap.data.remoteDataSource.models.SetPasswordRes
import mai.project.foodmap.data.remoteDataSource.models.SetUserImageReq
import mai.project.foodmap.data.remoteDataSource.models.SetUserImageRes
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

    @Headers("Content-Type: application/json")
    @POST("api/user/push_place_list")
    suspend fun pushPlaceList(
        @Body body: PushPlaceListReq
    ): Response<PushPlaceListRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/pull_place_list")
    suspend fun pullPlaceList(
        @Body pullPlaceListReq: PullPlaceListReq
    ): Response<PullPlaceListRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/get_place_list")
    suspend fun getPlaceList(
        @Body getPlaceListReq: GetPlaceListReq
    ): Response<GetPlaceListRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/push_favorite")
    suspend fun pushFavoriteList(
        @Body body: PushFavoriteListReq
    ): Response<PushFavoriteListRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/pull_favorite")
    suspend fun pullFavoriteList(
        @Body body: PullFavoriteListReq
    ): Response<PullFavoriteListRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/get_favorite")
    suspend fun getFavoriteList(
        @Body body: GetFavoriteListReq
    ): Response<GetFavoriteListRes>
    // endregion user

    // region place
    @Headers("Content-Type: application/json")
    @POST("api/place/draw_card")
    suspend fun getDrawCard(
        @Body body: DrawCardReq
    ): Response<DrawCardRes>
    // endregion place

    // region geocode
    @Headers("Content-Type: application/json")
    @POST("api/geocode/autocomplete")
    suspend fun placeAutoComplete(
        @Body body: PlaceAutocompleteReq
    ): Response<PlaceAutocompleteRes>

    @Headers("Content-Type: application/json")
    @POST("api/geocode/get_location_by_address")
    suspend fun getLocationByAddress(
        @Body body: GetLocationByAddressReq
    ): Response<GetLocationByAddressRes>
    // endregion geocode
}