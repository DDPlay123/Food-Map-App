package mai.project.foodmap.data.repositoryImpl

import kotlinx.coroutines.flow.firstOrNull
import mai.project.foodmap.data.annotations.DrawCardMode
import mai.project.foodmap.data.mapper.mapToRestaurantResults
import mai.project.foodmap.data.remoteDataSource.APIService
import mai.project.foodmap.data.remoteDataSource.models.DrawCardReq
import mai.project.foodmap.data.remoteDataSource.models.LocationModel
import mai.project.foodmap.data.utils.handleAPIResponse
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.repository.PlaceRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

internal class PlaceImpl @Inject constructor(
    private val apiService: APIService,
    private val preferenceRepo: PreferenceRepo
) : PlaceRepo {

    override suspend fun getDrawCard(
        lat: Double,
        lng: Double,
        @DrawCardMode
        mode: Int
    ): NetworkResult<List<RestaurantResult>> {
        val result = handleAPIResponse(
            apiService.getDrawCard(
                DrawCardReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    location = LocationModel(lat = lat, lng = lng),
                    mode = mode,
                    num = 10
                )
            )
        )
        return result.mapToRestaurantResults(preferenceRepo.readUserId.firstOrNull().orEmpty())
    }
}