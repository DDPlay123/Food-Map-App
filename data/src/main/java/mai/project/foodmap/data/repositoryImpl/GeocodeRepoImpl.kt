package mai.project.foodmap.data.repositoryImpl

import kotlinx.coroutines.flow.firstOrNull
import mai.project.foodmap.data.mapper.mapToRestaurantRouteResult
import mai.project.foodmap.data.mapper.mapToSearchPlaceResultWithPlaceAutocompleteRes
import mai.project.foodmap.data.mapper.mapToSearchPlaceResultWithGetLocationByAddressRes
import mai.project.foodmap.data.mapper.mapToSearchPlacesResultWithPlaceAutocompleteRes
import mai.project.foodmap.data.remoteDataSource.APIService
import mai.project.foodmap.data.remoteDataSource.models.GetLocationByAddressReq
import mai.project.foodmap.data.remoteDataSource.models.GetRoutePolylineReq
import mai.project.foodmap.data.remoteDataSource.models.LocationModel
import mai.project.foodmap.data.remoteDataSource.models.PlaceAutocompleteReq
import mai.project.foodmap.data.utils.handleAPIResponse
import mai.project.foodmap.domain.models.RestaurantRouteResult
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
        return result.mapToSearchPlaceResultWithPlaceAutocompleteRes()
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
        return result.mapToSearchPlacesResultWithPlaceAutocompleteRes()
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

    override suspend fun getRoute(
        originLat: Double,
        originLng: Double,
        targetPlaceId: String
    ): NetworkResult<RestaurantRouteResult> {
        val result = handleAPIResponse(
            apiService.getRoutePolyline(
                GetRoutePolylineReq(
                    userId = preferenceRepo.readUserId.firstOrNull().orEmpty(),
                    accessKey = preferenceRepo.readAccessKey.firstOrNull().orEmpty(),
                    origin = GetRoutePolylineReq.LocationInfo(
                        placeId = "",
                        lat = originLat,
                        lng = originLng
                    ),
                    destination = GetRoutePolylineReq.LocationInfo(
                        placeId = targetPlaceId,
                        lat = 0.0,
                        lng = 0.0
                    )
                )
            )
        )
        return result.mapToRestaurantRouteResult()
    }
}