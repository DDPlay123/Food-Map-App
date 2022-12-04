package com.side.project.foodmap.data.local.getFavorite

import androidx.lifecycle.LiveData
import androidx.room.*
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.util.Constants.GET_FAVORITE_MODEL

@Dao
interface GetFavoriteDao {

    @Query("SELECT * FROM $GET_FAVORITE_MODEL")
    fun getData(): LiveData<List<FavoriteList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(favoriteList: FavoriteList)

    @Delete
    fun deleteData(favoriteList: FavoriteList)

    @Query("DELETE FROM $GET_FAVORITE_MODEL")
    fun deleteAllData()
}