package com.side.project.foodmap.data.local.drawCard

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.remote.api.restaurant.DrawCardRes

@Database(entities = [DrawCardRes::class], version = 2, exportSchema = false)
@TypeConverters(DrawCardConverter::class)
abstract class DrawCardDb: RoomDatabase() {
    abstract fun drawCardDao(): DrawCardDao
}