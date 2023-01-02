package com.side.project.foodmap.data.local.drawCard

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.local.DrawCardConverter
import com.side.project.foodmap.data.remote.restaurant.DrawCardRes

@Database(entities = [DrawCardRes::class], version = 1, exportSchema = false)
@TypeConverters(DrawCardConverter::class)
abstract class DrawCardDb: RoomDatabase() {
    abstract fun drawCardDao(): DrawCardDao
}