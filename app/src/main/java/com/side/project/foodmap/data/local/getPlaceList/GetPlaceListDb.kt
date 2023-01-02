package com.side.project.foodmap.data.local.getPlaceList

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.local.LocationConverter
import com.side.project.foodmap.data.remote.MyPlaceList

@Database(entities = [MyPlaceList::class], version = 1, exportSchema = false)
@TypeConverters(LocationConverter::class)
abstract class GetPlaceListDb: RoomDatabase() {
    abstract fun getPlaceListDao(): GetPlaceListDao
}