package com.side.project.foodmap.data.local.drawCard

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.data.remote.api.restaurant.DrawCardRes

class DrawCardConverter {
    @TypeConverter
    fun fromModel(result: DrawCardRes.Result): String {
        val type = object : TypeToken<DrawCardRes.Result>() {}.type
        return Gson().toJson(result, type)
    }

    @TypeConverter
    fun toModel(drawCardString: String): DrawCardRes.Result {
        val type = object : TypeToken<DrawCardRes.Result>() {}.type
        return Gson().fromJson(drawCardString, type)
    }
}