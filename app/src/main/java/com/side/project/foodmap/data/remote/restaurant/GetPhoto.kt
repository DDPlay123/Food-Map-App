package com.side.project.foodmap.data.remote.restaurant

import com.side.project.foodmap.data.remote.BaseRequest
import com.side.project.foodmap.data.remote.BaseResponse

data class GetPhotoReq(
    override val accessKey: String,
    override val userId: String,
    val photoId: String,
    val detail: Boolean = false
) : BaseRequest()

data class GetPhotoRes(
    val result: Result
) : BaseResponse() {
    data class Result(
        val updateTime: String,
        val photo_reference: String? = null,
        val width: Int,
        val height: Int,
        val data: String,
        val length: Long,
        val format: String
    )
}