package com.side.project.foodmap.data.remote.google.placesAutoComplete

data class AutoComplete(
    val predictions: List<Prediction>,
    val status: String
)