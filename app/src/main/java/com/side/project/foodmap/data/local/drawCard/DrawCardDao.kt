package com.side.project.foodmap.data.local.drawCard

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.side.project.foodmap.data.remote.api.restaurant.DrawCardRes
import com.side.project.foodmap.util.Constants.DRAW_CARD_MODEL

@Dao
interface DrawCardDao {

    @Query("SELECT * FROM $DRAW_CARD_MODEL")
    fun getData(): DrawCardRes

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(drawCardRes: DrawCardRes)

    @Query("DELETE FROM $DRAW_CARD_MODEL")
    suspend fun deleteData()
}