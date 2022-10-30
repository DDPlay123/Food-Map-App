package com.side.project.foodmap.data.remote.google.placeDetails

data class OpeningHours(
    val open_now: Boolean,
    val periods: List<Period>,
    val weekday_text: List<String>
)