package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.remoteDataSource.models.SearchAutocompleteRes
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.SearchRestaurantResult
import mai.project.foodmap.domain.state.NetworkResult

internal fun NetworkResult<SearchAutocompleteRes>.mapToSearchRestaurantResult(): NetworkResult<List<SearchRestaurantResult>> {
    return mapResult { data ->
        data?.result?.placeList?.map {
            SearchRestaurantResult(
                placeCount = data.result.placeCount,
                placeId = it.placeId,
                name = it.name,
                address = it.address,
                description = it.description,
                isSearch = it.isSearch
            )
        }
    }
}