package mai.project.foodmap.data.localDataSource.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import mai.project.foodmap.data.localDataSource.MY_FAVORITE_RESTAURANT
import mai.project.foodmap.data.localDataSource.entities.MyFavoriteEntity

@Dao
internal interface MyFavoriteDao {

    @Query("SELECT * FROM $MY_FAVORITE_RESTAURANT")
    fun readMyFavoriteList(): Flow<List<MyFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyFavorite(myFavoriteEntity: MyFavoriteEntity)

    @Update
    suspend fun updateMyFavorite(myFavoriteEntity: MyFavoriteEntity)

    @Query("DELETE FROM $MY_FAVORITE_RESTAURANT WHERE `index` = :placeId")
    suspend fun deleteMyFavorite(placeId: String)

    @Delete
    suspend fun deleteMyFavorite(myFavoriteEntity: MyFavoriteEntity)

    @Transaction
    suspend fun syncMyFavoriteList(
        serverList: List<MyFavoriteEntity>
    ) {
        val localList = readMyFavoriteList().first().associateBy { it.index }

        localList.forEach { (placeId, entity) ->
            if (serverList.none { it.index == placeId }) {
                deleteMyFavorite(entity)
            }
        }

        serverList.forEach { newEntity ->
            val localEntity = localList[newEntity.index]
            if (localEntity != null) {
                updateMyFavorite(newEntity)
            } else {
                insertMyFavorite(newEntity)
            }
        }
    }
}