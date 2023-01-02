package com.side.project.foodmap.data.local.distanceSearch

import androidx.room.*
import com.side.project.foodmap.data.remote.restaurant.DistanceSearchRes
import com.side.project.foodmap.util.Constants.DISTANCE_SEARCH_MODEL

@Dao
interface DistanceSearchDao {

    @Query("SELECT * FROM $DISTANCE_SEARCH_MODEL")
    fun getData(): DistanceSearchRes

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(distanceSearchRes: DistanceSearchRes)

    @Query("DELETE FROM $DISTANCE_SEARCH_MODEL")
    suspend fun deleteData()
}