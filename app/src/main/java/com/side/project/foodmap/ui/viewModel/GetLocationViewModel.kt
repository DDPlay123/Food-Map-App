package com.side.project.foodmap.ui.viewModel

import com.side.project.foodmap.data.remote.Location

class GetLocationViewModel : BaseViewModel() {

    /**
     * 資料流
     */
    val locationAutoCompleteFlow get() = geocodeApiRepo.locationAutoCompleteFlow

    val keywordAutoCompleteFlow get() = geocodeApiRepo.keywordAutoCompleteFlow

    val getLocationFlow get() = geocodeApiRepo.getLocationFlow

    val pushPlaceListFlow get() = userApiRepo.pushPlaceListFlow

    /**
     * 可呼叫方法
     */
    fun autocompleteByLocation(
        location: Location
    ) = geocodeApiRepo.apiGeocodeAutocompleteByLocation(location)

    fun autocompleteByKeyword(
        location: Location,
        input: String
    ) = geocodeApiRepo.apiGeocodeAutocompleteByKeyword(location, input)

    fun getLocationByAddress(
        address: String
    ) = geocodeApiRepo.apiGeocodeGetLocationByAddress(address)

    fun pushPlaceList(
        place_id: String,
        name: String,
        address: String,
        location: Location
    ) = userApiRepo.apiPushPlaceList(place_id, name, address, location)
}