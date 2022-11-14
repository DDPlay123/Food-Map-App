package com.side.project.foodmap.network

import com.side.project.foodmap.data.remote.api.restaurant.*
import com.side.project.foodmap.data.remote.api.user.*
import com.side.project.foodmap.data.remote.google.placesAutoComplete.AutoComplete
import com.side.project.foodmap.data.remote.google.placesDetails.PlacesDetails
import com.side.project.foodmap.data.remote.google.placesSearch.PlacesSearch
import com.side.project.foodmap.data.remote.tdx.RestaurantList
import com.side.project.foodmap.data.remote.tdx.TdxTokenRes
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    /**
     * Restaurant API Server
     */
    @Headers("Content-Type: application/json")
    @POST("api/restaurant/update")
    fun apiRestaurantUpdate(@Body updateReq: UpdateReq): Call<UpdateRes>

    @Headers("Content-Type: application/json")
    @POST("api/restaurant/update")
    fun apiRestaurantNearSearch(@Body nearSearchReq: NearSearchReq): Call<NearSearchRes>

    @Headers("Content-Type: application/json")
    @POST("api/restaurant/update")
    fun apiRestaurantNameSearch(@Body nameSearchReq: NameSearchReq): Call<NameSearchRes>

    /**
     * User API Server
     */
    @Headers("Content-Type: application/json")
    @POST("api/user/login")
    fun apiUserLogin(@Body loginReq: LoginReq): Call<LoginRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/logout")
    fun apiUserLogout(@Body logoutReq: LogoutReq): Call<LogoutRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/register")
    fun apiUserRegister(@Body registerReq: RegisterReq): Call<RegisterRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/delete_account")
    fun apiDeleteAccount(@Body deleteAccountReq: DeleteAccountReq): Call<DeleteAccountRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/add_fcm_token")
    fun apiAddFcmToken(@Body addFcmTokenReq: AddFcmTokenReq): Call<AddFcmTokenRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/set_image")
    fun apiSetUserImage(@Body setUserImageReq: SetUserImageReq): Call<SetUserImageRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/get_image")
    fun apiGetUserImage(@Body getUserImageReq: GetUserImageReq): Call<GetUserImageRes>

    @Headers("Content-Type: application/json")
    @POST("api/user/set_password")
    fun apiSetUserPassword(@Body setPasswordReq: SetPasswordReq): Call<SetPasswordRes>

    /********************** 以下未用到 ************************/
    /**
     * TDX Token
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("protocol/openid-connect/token")
    fun getToken(
        @Field("grant_type") grant_type: String,
        @Field("client_id") client_id: String,
        @Field("client_secret") client_secret: String
    ): Call<TdxTokenRes>

    /**
     * 註：TDX Data header Map ->  authorization : Bearer ACCESS_TOKEN
     * 原因：TDX Token 是會改變的。
     */

    /**
     * TDX 全部餐飲資料 (目前無台北資料)
     */
    @GET("v2/Tourism/Restaurant")
    fun getAllRestaurantByTDX(
        @Header("authorization") token: String
    ): Call<RestaurantList>

    /**
     * TDX 指定縣市餐飲資料 (目前無台北資料)
     */
    @GET("v2/Tourism/Restaurant/{city}")
    fun getCityRestaurantByTDX(
        @Header("authorization") token: String,
        @Path("city") city: String
    ): Call<RestaurantList>

    /**
     * Google Places Auto Complete
     */
    @Headers("Accept-Encoding: identity")
    @GET("autocomplete/json")
    fun getPlacesAutoComplete(
        @Query("input") input: String,
//        @Query("location") location: String, // Ex: 25.0338,121.5646
        @Query("components") component: String = "country:tw",
//        @Query("radius") radius: String = "1000",
        @Query("type") type: String = "restaurant",
        @Query("key") key: String,
        @Query("language") language: String = "zh-TW"
    ): Call<AutoComplete>

    /**
     * Google Places Search
     */
    @Headers("Accept-Encoding: identity")
    @GET("nearbysearch/json")
    fun getPlaceSearch(
        @Query("location") location: String, // Ex: 25.0338,121.5646
        @Query("radius") radius: String = "1000", // Ex: 1000 公尺
        @Query("type") type: String = "restaurant",
        @Query("key") key: String,
        @Query("language") language: String = "zh-TW"
    ): Call<PlacesSearch>

    @Headers("Accept-Encoding: identity")
    @GET("nearbysearch/json")
    fun getPlaceSearch(
        @Query("location") location: String, // Ex: 25.0338,121.5646
        @Query("radius") radius: String = "1000", // Ex: 1000 公尺
        @Query("pagetoken") token: String,
        @Query("type") type: String = "restaurant",
        @Query("key") key: String,
        @Query("language") language: String = "zh-TW"
    ): Call<PlacesSearch>

    /**
     * Google Places Search with keyword
     */
    @Headers("Accept-Encoding: identity")
    @GET("nearbysearch/json")
    fun getPlaceSearchWithKeyword(
        @Query("location") location: String, // Ex: 25.0338,121.5646
        @Query("radius") radius: Long = 1000L, // Ex: 1000 公尺
        @Query("keyword") keyword: String,
        @Query("key") key: String,
        @Query("language") language: String = "zh-TW"
    ): Call<PlacesSearch>

    @Headers("Accept-Encoding: identity")
    @GET("nearbysearch/json")
    fun getPlaceSearchWithKeyword(
        @Query("location") location: String, // Ex: 25.0338,121.5646
        @Query("radius") radius: Long = 1000L, // Ex: 1000 公尺
        @Query("pagetoken") token: String,
        @Query("keyword") keyword: String,
        @Query("key") key: String,
        @Query("language") language: String = "zh-TW"
    ): Call<PlacesSearch>

    /**
     * Google Places Details
     */
    @Headers("Accept-Encoding: identity")
    @GET("details/json")
    fun getPlaceDetails(
        @Query("place_id") placeID: String,
        @Query("key") key: String
    ): Call<PlacesDetails>
}