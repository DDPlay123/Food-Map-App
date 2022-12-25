package com.side.project.foodmap.data.local.historySearch

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.side.project.foodmap.data.remote.api.HistorySearch
import com.side.project.foodmap.util.Constants.HISTORY_SEARCH

@Dao
interface HistorySearchDao {

    @Query("SELECT * FROM $HISTORY_SEARCH")
    fun getData(): List<HistorySearch>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(historySearch: HistorySearch)

    @Delete
    fun deleteData(historySearch: HistorySearch)

    @Query("DELETE FROM $HISTORY_SEARCH")
    fun deleteAllData()
}