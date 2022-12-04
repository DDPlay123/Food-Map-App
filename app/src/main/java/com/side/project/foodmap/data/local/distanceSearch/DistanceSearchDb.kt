package com.side.project.foodmap.data.local.distanceSearch

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes

@Database(entities = [DistanceSearchRes::class], version = 1, exportSchema = false)
@TypeConverters(DistanceSearchConverter::class)
abstract class DistanceSearchDb: RoomDatabase() {
    abstract fun distanceSearchDao(): DistanceSearchDao
}