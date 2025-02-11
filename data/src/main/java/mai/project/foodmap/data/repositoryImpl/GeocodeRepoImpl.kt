package mai.project.foodmap.data.repositoryImpl

import kotlinx.coroutines.flow.firstOrNull
import mai.project.foodmap.data.mapper.mapToSearchPlaceResult
import mai.project.foodmap.data.mapper.mapToSearchPlaceResultWithGetLocationByAddressRes
import mai.project.foodmap.data.mapper.mapToSearchPlacesResult
import mai.project.foodmap.data.remoteDataSource.APIService
import mai.project.foodmap.data.remoteDataSource.models.GetLocationByAddressReq
import mai.project.foodmap.data.remoteDataSource.models.LocationModel
import mai.project.foodmap.data.remoteDataSource.models.PlaceAutocompleteReq
import mai.project.foodmap.data.utils.handleAPIResponse
import mai.project.foodmap.domain.models.SearchPlaceResult
import mai.project.foodmap.domain.repository.GeocodeRepo
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

internal class GeocodeRepoImpl @Inject constructor(
    private val apiService: APIService,
    private val preferenceRepo: PreferenceRepo
): GeocodeRepo {

    override suspend fun searchPlacesByLocation(
        lat: Double,
        lng: Double
    ): NetworkResult<SearchPlaceResult> {
        val result = handleAPIResponse(
            apiService.placeAutoComplete(
                PlaceAutocompleteReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    location = LocationModel(lat = lat, lng = lng),
                    input = ""
                )
            )
        )
        return result.mapToSearchPlaceResult()
    }

    override suspend fun searchPlacesByKeyword(
        keyword: String,
        lat: Double,
        lng: Double
    ): NetworkResult<List<SearchPlaceResult>> {
        val result = handleAPIResponse(
            apiService.placeAutoComplete(
                PlaceAutocompleteReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    location = LocationModel(lat = lat, lng = lng),
                    input = keyword
                )
            )
        )
        return result.mapToSearchPlacesResult()
    }

    override suspend fun getPlaceByAddress(
        address: String
    ): NetworkResult<SearchPlaceResult> {
        val result = handleAPIResponse(
            apiService.getLocationByAddress(
                GetLocationByAddressReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    address = address
                )
            )
        )
        return result.mapToSearchPlaceResultWithGetLocationByAddressRes()
    }
}