package com.side.project.foodmap.data.remote.tdx

data class Position(
    val GeoHash: String,
    val PositionLat: Double,
    val PositionLon: Double
) {
    constructor(): this("", 0.00, 0.00)
}