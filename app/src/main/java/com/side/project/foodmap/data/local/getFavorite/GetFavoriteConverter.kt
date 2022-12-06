package com.side.project.foodmap.data.local.getFavorite

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.data.remote.api.Location

class GetFavoriteConverter {
    @TypeConverter
    fun fromModel(result: FavoriteList): String {
        val type = object : TypeToken<FavoriteList>() {}.type
        return Gson().toJson(result, type)
    }

    @TypeConverter
    fun toModel(getFavoriteList: String): FavoriteList {
        val type = object : TypeToken<FavoriteList>() {}.type
        return Gson().fromJson(getFavoriteList, type)
    }
}

class ListConverter {
    @TypeConverter
    fun fromModel(result: MutableList<String>): String {
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().toJson(result, type)
    }

    @TypeConverter
    fun toModel(getFavoriteList: String): MutableList<String> {
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(getFavoriteList, type)
    }
}

class LocationConverter {
    @TypeConverter
    fun fromModel(result: Location): String {
        val type = object : TypeToken<Location>() {}.type
        return Gson().toJson(result, type)
    }

    @TypeConverter
    fun toModel(getFavoriteList: String): Location {
        val type = object : TypeToken<Location>() {}.type
        return Gson().fromJson(getFavoriteList, type)
    }
}