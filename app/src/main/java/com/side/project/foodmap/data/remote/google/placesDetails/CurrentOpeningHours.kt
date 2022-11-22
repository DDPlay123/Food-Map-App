package com.side.project.foodmap.data.remote.google.placesDetails

data class CurrentOpeningHours(
    val open_now: Boolean,
    val periods: List<Period>,
    val weekday_text: List<String>
)