package com.side.project.foodmap.data.remote.tdx

data class RestaurantList(
    val restaurantList: List<Restaurant>
) {
    constructor() : this(emptyList())
}