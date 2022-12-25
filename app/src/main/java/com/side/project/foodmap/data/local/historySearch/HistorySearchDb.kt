package com.side.project.foodmap.data.local.historySearch

import androidx.room.Database
import androidx.room.RoomDatabase
import com.side.project.foodmap.data.remote.api.HistorySearch

@Database(entities = [HistorySearch::class], version = 2, exportSchema = false)
abstract class HistorySearchDb: RoomDatabase() {
    abstract fun getHistorySearchDao(): HistorySearchDao
}