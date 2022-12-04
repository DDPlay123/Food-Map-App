package com.side.project.foodmap.data.local.distanceSearch

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes

class DistanceSearchConverter {
    @TypeConverter
    fun fromModel(result: DistanceSearchRes.Result): String {
        val type = object : TypeToken<DistanceSearchRes.Result>() {}.type
        return Gson().toJson(result, type)
    }

    @TypeConverter
    fun toModel(distanceSearchString: String): DistanceSearchRes.Result {
        val type = object : TypeToken<DistanceSearchRes.Result>() {}.type
        return Gson().fromJson(distanceSearchString, type)
    }
}