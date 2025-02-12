package mai.project.foodmap.domain.repository

import mai.project.foodmap.domain.models.RestaurantDetailResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.models.SearchPlaceResult
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Singleton

@Singleton
interface PlaceRepo {

    /**
     * 抽取人氣餐廳卡片
     */
    suspend fun getDrawCard(
        lat: Double,
        lng: Double,
        mode: Int
    ): NetworkResult<List<RestaurantResult>>

    /**
     * 取得餐廳詳細資訊
     */
    suspend fun getPlaceDetail(
        placeId: String
    ): NetworkResult<RestaurantDetailResult>

    /**
     * 使用附近地區 搜尋餐廳列表
     */
    suspend fun searchPlacesByDistance(
        lat: Double,
        lng: Double,
        distance: Int,
        skip: Int,
        limit: Int
    ): NetworkResult<List<RestaurantResult>>

    /**
     * 使用關鍵字 搜尋餐廳列表
     */
    suspend fun searchPlacesByKeyword(
        keyword: String,
        lat: Double,
        lng: Double,
        distance: Int,
        skip: Int,
        limit: Int
    ): NetworkResult<List<RestaurantResult>>

    /**
     * 使用關鍵字 搜尋相關餐廳資訊列表
     */
    suspend fun searchSamePlacesByKeyword(
        keyword: String,
        lat: Double,
        lng: Double,
        distance: Int
    ): NetworkResult<List<SearchPlaceResult>>
}