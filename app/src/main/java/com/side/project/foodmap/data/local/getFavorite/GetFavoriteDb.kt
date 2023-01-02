package com.side.project.foodmap.data.local.getFavorite

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.local.ListConverter
import com.side.project.foodmap.data.local.LocationConverter
import com.side.project.foodmap.data.remote.FavoriteList

@Database(entities = [FavoriteList::class], version = 1, exportSchema = false)
@TypeConverters(ListConverter::class, LocationConverter::class)
abstract class GetFavoriteDb: RoomDatabase() {
    abstract fun getFavoriteDao(): GetFavoriteDao
}