package mai.project.foodmap.data.localDataSource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mai.project.foodmap.data.localDataSource.MY_SEARCH_RESTAURANT
import mai.project.foodmap.data.localDataSource.entities.MySearchEntity

@Dao
internal interface MySearchDap {

    @Query("SELECT * FROM $MY_SEARCH_RESTAURANT")
    fun readMySearchList(): Flow<List<MySearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMySearch(mySearchEntity: MySearchEntity)

    @Query("DELETE FROM $MY_SEARCH_RESTAURANT WHERE `index` = :placeId")
    suspend fun deleteMySearch(placeId: String)

    @Query("DELETE FROM $MY_SEARCH_RESTAURANT")
    suspend fun deleteAllMySearch()
}