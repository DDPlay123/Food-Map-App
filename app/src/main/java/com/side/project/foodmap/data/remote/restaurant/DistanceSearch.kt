package com.side.project.foodmap.data.remote.restaurant

import androidx.room.Entity
import com.side.project.foodmap.data.remote.*
import com.side.project.foodmap.util.Constants.DISTANCE_SEARCH_MODEL
import java.io.Serializable

data class DistanceSearchReq(
    override val accessKey: String,
    override val userId: String,
    val location: Location,
    val distance: Int = -1,
    val skip: Int = 0,
    val limit: Int = 100
) : BaseRequest()

@Entity(tableName = DISTANCE_SEARCH_MODEL, primaryKeys = ["result"])
class DistanceSearchRes(
    val result: Result
) : BaseResponse(), Serializable {
    data class Result(
        val msg: String? = null,
        val updated: Boolean,
        val placeCount: Int,
        val placeList: ArrayList<PlaceList>
    )
}

