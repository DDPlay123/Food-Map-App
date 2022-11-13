package com.side.project.foodmap.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    /**
     * HTTP 攔截器
     */
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(LogInterceptor())
        .build()

    /**
     * API Server
     */
    val getAPI: ApiService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("http://kkhomeserver.ddns.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * 取得TDX Token
     */
    val getTdxToken: ApiService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://tdx.transportdata.tw/auth/realms/TDXConnect/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * 取得TDX 餐飲資料(台北目前未提供)
     */
    val getTdxRestaurant: ApiService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://tdx.transportdata.tw/api/basic/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Google Places API
     */
    val googlePlaces: ApiService by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}