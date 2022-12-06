package com.side.project.foodmap.data.remote.google.placesDetails

data class PlacesDetails(
    val html_attributions: List<Any>,
    val result: Result,
    val status: String,
    val isFavorite: Boolean
)