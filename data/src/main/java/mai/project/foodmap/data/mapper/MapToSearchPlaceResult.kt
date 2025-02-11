package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.remoteDataSource.models.GetLocationByAddressRes
import mai.project.foodmap.data.remoteDataSource.models.PlaceAutocompleteRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.SearchPlaceResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<PlaceAutocompleteRes>.mapToSearchPlacesResult(): NetworkResult<List<SearchPlaceResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            SearchPlaceResult(
                placeCount = data.result.placeCount,
                placeId = it.placeId,
                name = it.name,
                address = it.address,
                description = it.description,
                lat = it.location?.lat,
                lng = it.location?.lng
            )
        }
    }
}

internal fun NetworkResult<GetLocationByAddressRes>.mapToSearchPlaceResult(): NetworkResult<SearchPlaceResult> {
    return mapResult { data ->
        data?.result?.place?.let {
            SearchPlaceResult(
                placeCount = -1,
                placeId = it.placeId,
                name = it.name,
                address = it.address,
                description = it.description,
                lat = it.location?.lat,
                lng = it.location?.lng
            )
        }
    }
}