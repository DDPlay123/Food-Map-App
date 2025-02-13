package mai.project.foodmap.data.repositoryImpl

import kotlinx.coroutines.flow.firstOrNull
import mai.project.foodmap.data.annotations.DrawCardMode
import mai.project.foodmap.data.mapper.mapToRestaurantDetailResult
import mai.project.foodmap.data.mapper.mapToRestaurantResultsWithDrawCardRes
import mai.project.foodmap.data.mapper.mapToRestaurantResultsWithSearchByDistanceRes
import mai.project.foodmap.data.mapper.mapToRestaurantResultsWithSearchByKeywordRes
import mai.project.foodmap.data.mapper.mapToSearchPlacesResultWithSearchAutocompleteRes
import mai.project.foodmap.data.remoteDataSource.APIService
import mai.project.foodmap.data.remoteDataSource.models.DrawCardReq
import mai.project.foodmap.data.remoteDataSource.models.GetPlaceDetailReq
import mai.project.foodmap.data.remoteDataSource.models.LocationModel
import mai.project.foodmap.data.remoteDataSource.models.SearchAutocompleteReq
import mai.project.foodmap.data.remoteDataSource.models.SearchByDistanceReq
import mai.project.foodmap.data.remoteDataSource.models.SearchByKeywordReq
import mai.project.foodmap.data.utils.handleAPIResponse
import mai.project.foodmap.data.utils.safeIoWorker
import mai.project.foodmap.domain.models.RestaurantDetailResult
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.models.SearchPlaceResult
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

        result.data?.result?.placeList
            ?.filter { it.isFavorite }
            ?.map { it.placeId }
            ?.forEach { preferenceRepo.addMyFavoritePlaceId(it) }

        return result.mapToRestaurantResultsWithDrawCardRes(preferenceRepo.readUserId.firstOrNull().orEmpty())
    }

    override suspend fun getPlaceDetail(placeId: String): NetworkResult<RestaurantDetailResult> {
        val result = handleAPIResponse(
            apiService.getPlaceDetail(
                GetPlaceDetailReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    placeId = placeId
                )
            )
        )

        if (result.data?.result?.isFavorite == true) safeIoWorker {
            preferenceRepo.addMyFavoritePlaceId(placeId)
        }

        return result.mapToRestaurantDetailResult(preferenceRepo.readUserId.firstOrNull().orEmpty())
    }

    override suspend fun searchPlacesByDistance(
        lat: Double,
        lng: Double,
        distance: Int,
        skip: Int,
        limit: Int
    ): NetworkResult<List<RestaurantResult>> {
        val result = handleAPIResponse(
            apiService.searchByDistance(
                SearchByDistanceReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    location = LocationModel(lat = lat, lng = lng),
                    distance = distance,
                    skip = skip,
                    limit = limit
                )
            )
        )

        result.data?.result?.placeList
            ?.filter { it.isFavorite }
            ?.map { it.placeId }
            ?.forEach { preferenceRepo.addMyFavoritePlaceId(it) }

        return result.mapToRestaurantResultsWithSearchByDistanceRes(preferenceRepo.readUserId.firstOrNull().orEmpty())
    }

    override suspend fun searchPlacesByKeyword(
        keyword: String,
        lat: Double,
        lng: Double,
        distance: Int,
        skip: Int,
        limit: Int
    ): NetworkResult<List<RestaurantResult>> {
        val result = handleAPIResponse(
            apiService.searchByKeyword(
                SearchByKeywordReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    location = LocationModel(lat = lat, lng = lng),
                    distance = distance,
                    keyword = keyword,
                    skip = skip,
                    limit = limit
                )
            )
        )

        result.data?.result?.placeList
            ?.filter { it.isFavorite }
            ?.map { it.placeId }
            ?.forEach { preferenceRepo.addMyFavoritePlaceId(it) }

        return result.mapToRestaurantResultsWithSearchByKeywordRes(preferenceRepo.readUserId.firstOrNull().orEmpty())
    }

    override suspend fun searchSamePlacesByKeyword(
        keyword: String,
        lat: Double,
        lng: Double,
        distance: Int
    ): NetworkResult<List<SearchPlaceResult>> {
        val result = handleAPIResponse(
            apiService.searchAutocomplete(
                SearchAutocompleteReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    location = LocationModel(lat = lat, lng = lng),
                    distance = distance.toLong(),
                    input = keyword
                )
            )
        )

        return result.mapToSearchPlacesResultWithSearchAutocompleteRes()
    }
}