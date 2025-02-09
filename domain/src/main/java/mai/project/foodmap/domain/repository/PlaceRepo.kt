package mai.project.foodmap.domain.repository

import mai.project.foodmap.domain.models.RestaurantResult
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
}