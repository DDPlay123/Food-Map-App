package com.side.project.foodmap.data.remote.tdx

data class Restaurant(
    val Address: String,
    val City: String,
    val Class: String,
    val Description: String,
    val MapUrl: String,
    val OpenTime: String,
    val ParkingInfo: String,
    val Phone: String,
    val Picture: Picture?,
    val Position: Position?,
    val RestaurantID: String,
    val RestaurantName: String,
    val SrcUpdateTime: String,
    val UpdateTime: String,
    val WebsiteUrl: String,
    val ZipCode: String
) {
    constructor() : this("", "", "", "", "", "", "", "", null, null, "", "", "", "", "", "")
}