package com.side.project.foodmap.data.remote.api.restaurant

import com.side.project.foodmap.data.remote.api.BaseRequest
import com.side.project.foodmap.data.remote.api.BaseResponse
import com.side.project.foodmap.data.remote.api.HistorySearch

data class AutoCompleteReq(
    override val accessKey: String,
    override val userId: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Long, // 公尺
    val input: String
) : BaseRequest()

data class AutoCompleteRes(
    val result: List<HistorySearch>
) : BaseResponse()