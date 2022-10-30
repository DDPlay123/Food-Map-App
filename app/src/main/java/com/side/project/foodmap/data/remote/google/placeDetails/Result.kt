package com.side.project.foodmap.data.remote.google.placeDetails

data class Result(
    val address_components: List<AddressComponent>,
    val adr_address: String,
    val business_status: String,
    val formatted_address: String,
    val formatted_phone_number: String,
    val geometry: Geometry,
    val icon: String,
    val icon_background_color: String,
    val icon_mask_base_uri: String,
    val international_phone_number: String,
    val name: String,
    val opening_hours: OpeningHours,
    val photos: List<Photo>,
    val place_id: String,
    val plus_code: PlusCode,
    val rating: Int,
    val reference: String,
    val reviews: List<Review>,
    val types: List<String>,
    val url: String,
    val user_ratings_total: Int,
    val utc_offset: Int,
    val vicinity: String,
    val website: String
)