package com.side.project.foodmap.data.remote.tdx

data class Picture(
    val PictureDescription1: String,
    val PictureDescription2: String,
    val PictureDescription3: String,
    val PictureUrl1: String,
    val PictureUrl2: String,
    val PictureUrl3: String
) {
    constructor(): this("", "", "", "", "", "")
}