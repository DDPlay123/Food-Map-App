package mai.project.foodmap.domain.repository

import mai.project.foodmap.domain.models.SearchPlaceResult
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Singleton

@Singleton
interface GeocodeRepo {

    /**
     * 使用經緯度搜尋地區
     */
    suspend fun searchPlacesByLocation(
        lat: Double,
        lng: Double
    ): NetworkResult<List<SearchPlaceResult>>

    /**
     * 使用關鍵字和經緯度搜尋地區
     */
    suspend fun searchPlacesByKeyword(
        keyword: String,
        lat: Double,
        lng: Double
    ): NetworkResult<List<SearchPlaceResult>>

    /**
     * 使用關鍵字搜尋地區資訊
     */
    suspend fun getPlaceByAddress(
        address: String
    ): NetworkResult<SearchPlaceResult>
}