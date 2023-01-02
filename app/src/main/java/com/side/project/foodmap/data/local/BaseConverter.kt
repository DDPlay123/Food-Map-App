package com.side.project.foodmap.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.data.remote.Icon
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.OpeningHours
import com.side.project.foodmap.data.remote.Rating
import com.side.project.foodmap.data.remote.restaurant.DistanceSearchRes
import com.side.project.foodmap.data.remote.restaurant.DrawCardRes

class DistanceSearchConverter {
    @TypeConverter
    fun fromModel(data: DistanceSearchRes.Result): String {
        val type = object : TypeToken<DistanceSearchRes.Result>() {}.type
        return Gson().toJson(data, type)
    }

    @TypeConverter
    fun toModel(data: String): DistanceSearchRes.Result {
        val type = object : TypeToken<DistanceSearchRes.Result>() {}.type
        return Gson().fromJson(data, type)
    }
}

class DrawCardConverter {
    @TypeConverter
    fun fromModel(data: DrawCardRes.Result): String {
        val type = object : TypeToken<DrawCardRes.Result>() {}.type
        return Gson().toJson(data, type)
    }

    @TypeConverter
    fun toModel(data: String): DrawCardRes.Result {
        val type = object : TypeToken<DrawCardRes.Result>() {}.type
        return Gson().fromJson(data, type)
    }
}

class ListConverter {
    @TypeConverter
    fun fromModel(data: MutableList<String>): String {
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().toJson(data, type)
    }

    @TypeConverter
    fun toModel(data: String): MutableList<String> {
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(data, type)
    }
}

class LocationConverter {
    @TypeConverter
    fun fromModel(data: Location): String {
        val type = object : TypeToken<Location>() {}.type
        return Gson().toJson(data, type)
    }

    @TypeConverter
    fun toModel(data: String): Location {
        val type = object : TypeToken<Location>() {}.type
        return Gson().fromJson(data, type)
    }
}

class RatingConverter {
    @TypeConverter
    fun fromModel(data: Rating): String {
        val type = object : TypeToken<Rating>() {}.type
        return Gson().toJson(data, type)
    }

    @TypeConverter
    fun toModel(data: String): Rating {
        val type = object : TypeToken<Rating>() {}.type
        return Gson().fromJson(data, type)
    }
}

class IconConverter {
    @TypeConverter
    fun fromModel(data: Icon): String {
        val type = object : TypeToken<Icon>() {}.type
        return Gson().toJson(data, type)
    }

    @TypeConverter
    fun toModel(data: String): Icon {
        val type = object : TypeToken<Icon>() {}.type
        return Gson().fromJson(data, type)
    }
}

class OpeningHoursConverter {
    @TypeConverter
    fun fromModel(data: OpeningHours): String {
        val type = object : TypeToken<OpeningHours>() {}.type
        return Gson().toJson(data, type)
    }

    @TypeConverter
    fun toModel(data: String): OpeningHours {
        val type = object : TypeToken<OpeningHours>() {}.type
        return Gson().fromJson(data, type)
    }
}