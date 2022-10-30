package com.side.project.foodmap.data.remote.google.nearBySearch

data class NearBySearch(
    val html_attributions: List<Any>,
    val results: List<Result>,
    val status: String
)