package com.side.project.foodmap.network

import com.side.project.foodmap.data.remote.tdx.RestaurantList
import com.side.project.foodmap.data.remote.tdx.TdxTokenRes
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    /**
     * TDX Token
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("protocol/openid-connect/token")
    fun getToken(@Field("grant_type") grant_type: String, @Field("client_id") client_id: String, @Field("client_secret") client_secret: String): Call<TdxTokenRes>

    /**
     * 註：TDX Data header Map ->  authorization : Bearer ACCESS_TOKEN
     * 原因：TDX Token 是會改變的。
     */

    /**
     * TDX 全部餐飲資料 (目前無台北資料)
     */
    @GET("v2/Tourism/Restaurant")
    fun getAllRestaurantByTDX(@Header("authorization") token: String): Call<RestaurantList>

    /**
     * TDX 指定縣市餐飲資料 (目前無台北資料)
     */
    @GET("v2/Tourism/Restaurant/{city}")
    fun getCityRestaurantByTDX(@Header("authorization") token: String, @Path("city") city: String): Call<RestaurantList>
}