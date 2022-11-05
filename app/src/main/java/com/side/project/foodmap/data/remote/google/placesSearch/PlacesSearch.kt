package com.side.project.foodmap.data.remote.google.placesSearch

data class PlacesSearch(
    val html_attributions: List<Any>,
    val next_page_token: String,
    val results: List<Result>,
    val status: String
)