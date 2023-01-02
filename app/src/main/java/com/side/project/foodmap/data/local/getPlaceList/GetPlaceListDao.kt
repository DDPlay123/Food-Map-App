package com.side.project.foodmap.data.local.getPlaceList

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.side.project.foodmap.data.remote.MyPlaceList
import com.side.project.foodmap.util.Constants.GET_PLACE_LIST_MODEL

@Dao
interface GetPlaceListDao {

    @Query("SELECT * FROM $GET_PLACE_LIST_MODEL")
    fun getData(): List<MyPlaceList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(placeList: MyPlaceList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllData(placeLists: List<MyPlaceList>)

    @Delete
    fun deleteData(placeList: MyPlaceList)

    @Query("DELETE FROM $GET_PLACE_LIST_MODEL")
    fun deleteAllData()
}