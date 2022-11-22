package com.side.project.foodmap.data.remote.google.placesDetails

data class OpeningHours(
    val open_now: Boolean,
    val periods: List<PeriodX>,
    val weekday_text: List<String>
)