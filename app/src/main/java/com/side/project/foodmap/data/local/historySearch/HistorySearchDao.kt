package com.side.project.foodmap.data.local.historySearch

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.side.project.foodmap.data.remote.AutoComplete
import com.side.project.foodmap.util.Constants.HISTORY_SEARCH_MODEL

@Dao
interface HistorySearchDao {

    @Query("SELECT * FROM $HISTORY_SEARCH_MODEL")
    fun getData(): List<AutoComplete>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(historySearch: AutoComplete)

    @Delete
    fun deleteData(historySearch: AutoComplete)

    @Query("DELETE FROM $HISTORY_SEARCH_MODEL")
    fun deleteAllData()
}