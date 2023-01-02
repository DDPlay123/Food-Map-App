package com.side.project.foodmap.data.local.historySearch

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.side.project.foodmap.data.local.LocationConverter
import com.side.project.foodmap.data.remote.AutoComplete

@Database(entities = [AutoComplete::class], version = 1, exportSchema = false)
@TypeConverters(LocationConverter::class)
abstract class HistorySearchDb: RoomDatabase() {
    abstract fun getHistorySearchDao(): HistorySearchDao
}