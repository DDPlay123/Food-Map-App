package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse
import com.side.project.foodmap.data.remote.google.placesDetails.PlacesDetails

data class DetailsByPlaceIdReq(
    override val accessKey: String,
    override val userId: String,
    val place_id: String
) : BaseRequest()

data class DetailsByPlaceIdRes(
    val result: PlacesDetails? = null
) : BaseResponse()