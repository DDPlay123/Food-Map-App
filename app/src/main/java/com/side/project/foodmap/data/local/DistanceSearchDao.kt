package com.side.project.foodmap.data.local

import androidx.room.*
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.util.Constants

@Dao
interface DistanceSearchDao {

    @Query("SELECT * FROM ${Constants.DISTANCE_SEARCH_MODEL}")
    fun getData(): DistanceSearchRes

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(distanceSearchRes: DistanceSearchRes)

    @Delete
    suspend fun deleteData(distanceSearchRes: DistanceSearchRes)
}