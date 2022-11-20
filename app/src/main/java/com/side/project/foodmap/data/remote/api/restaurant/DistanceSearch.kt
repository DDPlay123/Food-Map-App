package com.side.project.foodmap.data.remote.api.restaurant

import androidx.room.Entity
import com.side.project.foodmap.data.remote.api.*
import com.side.project.foodmap.util.Constants
import java.io.Serializable

data class DistanceSearchReq(
    override val accessKey: String,
    override val userId: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int = 100,
    val minNum: Int = 20,
    val maxNum: Int = 100
) : BaseRequest()

@Entity(tableName = Constants.DISTANCE_SEARCH_MODEL, primaryKeys = ["result"])
class DistanceSearchRes(
    val result: Result
) : BaseResponse(), Serializable {
    data class Result(
        val updated: Boolean,
        val placeCount: Long,
        val placeList: ArrayList<PlaceList>
    )
}

