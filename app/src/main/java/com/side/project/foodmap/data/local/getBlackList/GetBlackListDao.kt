package com.side.project.foodmap.data.local.getBlackList

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.side.project.foodmap.data.remote.PlaceList
import com.side.project.foodmap.util.Constants.GET_BLACK_LIST_MODEL

@Dao
interface GetBlackListDao {

    @Query("SELECT * FROM $GET_BLACK_LIST_MODEL")
    fun getData(): List<PlaceList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(placeList: PlaceList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(placeLists: List<PlaceList>)

    @Delete
    fun deleteData(placeList: PlaceList)

    @Query("DELETE FROM $GET_BLACK_LIST_MODEL")
    fun deleteAllData()
}