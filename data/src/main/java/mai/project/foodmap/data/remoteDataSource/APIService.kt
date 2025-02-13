package mai.project.foodmap.data.remoteDataSource

import mai.project.foodmap.data.remoteDataSource.models.DrawCardReq
import mai.project.foodmap.data.remoteDataSource.models.DrawCardRes
import mai.project.foodmap.data.remoteDataSource.models.AddFcmTokenReq
import mai.project.foodmap.data.remoteDataSource.models.AddFcmTokenRes
import mai.project.foodmap.data.remoteDataSource.models.DeleteAccountReq
import mai.project.foodmap.data.remoteDataSource.models.DeleteAccountRes
import mai.project.foodmap.data.remoteDataSource.models.GetBlacklistReq
import mai.project.foodmap.data.remoteDataSource.models.GetBlacklistRes
import mai.project.foodmap.data.remoteDataSource.models.GetFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.GetFavoriteListRes
import mai.project.foodmap.data.remoteDataSource.models.GetLocationByAddressReq
import mai.project.foodmap.data.remoteDataSource.models.GetLocationByAddressRes
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceDetailReq
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceDetailRes
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceListRes
import mai.project.foodmap.data.remoteDataSource.models.GetRoutePolylineReq
import mai.project.foodmap.data.remoteDataSource.models.GetRoutePolylineRes
import mai.project.foodmap.data.remoteDataSource.models.GetUserImageReq
import mai.project.foodmap.data.remoteDataSource.models.GetUserImageRes
import mai.project.foodmap.data.remoteDataSource.models.LoginReq
import mai.project.foodmap.data.remoteDataSource.models.LoginRes
import mai.project.foodmap.data.remoteDataSource.models.LogoutReq
import mai.project.foodmap.data.remoteDataSource.models.LogoutRes
import mai.project.foodmap.data.remoteDataSource.models.PlaceAutocompleteReq
import mai.project.foodmap.data.remoteDataSource.models.PlaceAutocompleteRes
import mai.project.foodmap.data.remoteDataSource.models.PullBlacklistReq
import mai.project.foodmap.data.remoteDataSource.models.PullBlacklistRes
import mai.project.foodmap.data.remoteDataSource.models.PullFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.PullFavoriteListRes
import mai.project.foodmap.data.remoteDataSource.models.PullPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.PullPlaceListRes
import mai.project.foodmap.data.remoteDataSource.models.PushBlacklistReq
import mai.project.foodmap.data.remoteDataSource.models.PushBlacklistRes
import mai.project.foodmap.data.remoteDataSource.models.PushFavoriteListReq
import mai.project.foodmap.data.remoteDataSource.models.PushFavoriteListRes
import mai.project.foodmap.data.remoteDataSource.models.PushPlaceListReq
import mai.project.foodmap.data.remoteDataSource.models.PushPlaceListRes
import mai.project.foodmap.data.remoteDataSource.models.RegisterReq
import mai.project.foodmap.data.remoteDataSource.models.RegisterRes
import mai.project.foodmap.data.remoteDataSource.models.SearchAutocompleteReq
import mai.project.foodmap.data.remoteDataSource.models.SearchAutocompleteRes
import mai.project.foodmap.data.remoteDataSource.models.SearchByDistanceReq
import mai.project.foodmap.data.remoteDataSource.models.SearchByDistanceRes
import mai.project.foodmap.data.remoteDataSource.models.SearchByKeywordReq
import mai.project.foodmap.data.remoteDataSource.models.SearchByKeywordRes
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

    @Headers("Content-Type: application/json")
    @POST("api/user/push_black_list")
    suspend fun pushBlacklist(
        @Body body: PushBlacklistReq
    ): Response<PushBlacklistRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/pull_black_list")
    suspend fun pullBlacklist(
        @Body body: PullBlacklistReq
    ): Response<PullBlacklistRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/get_black_list")
    suspend fun getBlacklist(
        @Body body: GetBlacklistReq
    ): Response<GetBlacklistRes>
    // endregion user

    // region place
    @Headers("Content-Type: application/json")
    @POST("api/place/draw_card")
    suspend fun getDrawCard(
        @Body body: DrawCardReq
    ): Response<DrawCardRes>

    @Headers("Content-Type: application/json")
    @POST("api/place/details_by_place_id")
    suspend fun getPlaceDetail(
        @Body body: GetPlaceDetailReq
    ): Response<GetPlaceDetailRes>

    @Headers("Content-Type: application/json")
    @POST("api/place/search_by_distance")
    suspend fun searchByDistance(
        @Body body: SearchByDistanceReq
    ): Response<SearchByDistanceRes>

    @Headers("Content-Type: application/json")
    @POST("api/place/search_by_keyword")
    suspend fun searchByKeyword(
        @Body body: SearchByKeywordReq
    ): Response<SearchByKeywordRes>

    @Headers("Content-Type: application/json")
    @POST("api/place/autocomplete")
    suspend fun searchAutocomplete(
        @Body body: SearchAutocompleteReq
    ): Response<SearchAutocompleteRes>
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

    @Headers("Content-Type: application/json")
    @POST("api/geocode/get_route_polyline")
    suspend fun getRoutePolyline(
        @Body body: GetRoutePolylineReq
    ): Response<GetRoutePolylineRes>
    // endregion geocode
}