package com.side.project.foodmap.data.local.getBlackList

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.local.*
import com.side.project.foodmap.data.remote.PlaceList

@Database(entities = [PlaceList::class], version = 1, exportSchema = false)
@TypeConverters(ListConverter::class, LocationConverter::class, RatingConverter::class, IconConverter::class, OpeningHoursConverter::class)
abstract class GetBlackListDb: RoomDatabase() {
    abstract fun getBlackListDao(): GetBlackListDao
}