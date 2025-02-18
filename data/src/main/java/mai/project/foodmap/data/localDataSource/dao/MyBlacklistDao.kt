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
import mai.project.foodmap.data.localDataSource.MY_BLOCKED_RESTAURANT
import mai.project.foodmap.data.localDataSource.entities.MyBlacklistEntity

@Dao
internal interface MyBlacklistDao {

    @Query("SELECT * FROM $MY_BLOCKED_RESTAURANT")
    fun readMyBlackList(): Flow<List<MyBlacklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyBlocked(myBlacklistEntity: MyBlacklistEntity)

    @Update
    suspend fun updateMyBlocked(myBlacklistEntity: MyBlacklistEntity)

    @Query("DELETE FROM $MY_BLOCKED_RESTAURANT WHERE `index` = :placeId")
    suspend fun deleteMyBlocked(placeId: String)

    @Delete
    suspend fun deleteMyBlocked(myBlacklistEntity: MyBlacklistEntity)

    @Transaction
    suspend fun syncMyBlacklist(
        serverList: List<MyBlacklistEntity>
    ) {
        val localList = readMyBlackList().first().associateBy { it.index }

        localList.forEach { (placeId, entity) ->
            if (serverList.none { it.index == placeId }) {
                deleteMyBlocked(entity)
            }
        }

        serverList.forEach { newEntity ->
            val localEntity = localList[newEntity.index]
            if (localEntity != null) {
                updateMyBlocked(newEntity)
            } else {
                insertMyBlocked(newEntity)
            }
        }
    }
}