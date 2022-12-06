package com.side.project.foodmap.data.local.getFavorite

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.data.remote.api.user.GetFavoriteRes

@Database(entities = [FavoriteList::class], version = 2, exportSchema = false)
@TypeConverters(GetFavoriteConverter::class, ListConverter::class, LocationConverter::class)
abstract class GetFavoriteDb: RoomDatabase() {
    abstract fun getFavoriteDao(): GetFavoriteDao
}