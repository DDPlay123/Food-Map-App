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
import mai.project.foodmap.data.localDataSource.MY_BLOCK_RESTAURANT
import mai.project.foodmap.data.localDataSource.entities.MyBlockEntity

@Dao
internal interface MyBlockDao {

    @Query("SELECT * FROM $MY_BLOCK_RESTAURANT")
    fun readMyBlockList(): Flow<List<MyBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyBlock(myBlockEntity: MyBlockEntity)

    @Update
    suspend fun updateMyBlock(myBlockEntity: MyBlockEntity)

    @Query("DELETE FROM $MY_BLOCK_RESTAURANT WHERE `index` = :placeId")
    suspend fun deleteMyBlock(placeId: String)

    @Delete
    suspend fun deleteMyBlock(myBlockEntity: MyBlockEntity)

    @Transaction
    suspend fun syncMyBlockList(
        serverList: List<MyBlockEntity>
    ) {
        val localList = readMyBlockList().first().associateBy { it.index }

        localList.forEach { (placeId, entity) ->
            if (serverList.none { it.index == placeId }) {
                deleteMyBlock(entity)
            }
        }

        serverList.forEach { newEntity ->
            val localEntity = localList[newEntity.index]
            if (localEntity != null) {
                updateMyBlock(newEntity)
            } else {
                insertMyBlock(newEntity)
            }
        }
    }
}