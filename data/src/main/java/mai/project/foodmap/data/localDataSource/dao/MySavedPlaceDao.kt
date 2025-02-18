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
import mai.project.foodmap.data.localDataSource.MY_SAVED_PLACE
import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity

@Dao
internal interface MySavedPlaceDao {

    @Query("SELECT * FROM $MY_SAVED_PLACE")
    fun readMySavedPlaceList(): Flow<List<MySavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMySavedPlace(mySavedPlaceEntity: MySavedPlaceEntity)

    @Update
    suspend fun updateMySavedPlace(mySavedPlaceEntity: MySavedPlaceEntity)

    @Query("DELETE FROM $MY_SAVED_PLACE WHERE `index` = :placeId")
    suspend fun deleteMySavedPlace(placeId: String)

    @Delete
    suspend fun deleteMySavedPlace(mySavedPlaceEntity: MySavedPlaceEntity)

    @Transaction
    suspend fun syncMySavedPlaceList(
        serverList: List<MySavedPlaceEntity>
    ) {
        val localList = readMySavedPlaceList().first().associateBy { it.index }

        localList.forEach { (placeId, entity) ->
            if (serverList.none { it.index == placeId }) {
                deleteMySavedPlace(entity)
            }
        }

        serverList.forEach { newEntity ->
            val localEntity = localList[newEntity.index]
            if (localEntity != null) {
                updateMySavedPlace(newEntity)
            } else {
                insertMySavedPlace(newEntity)
            }
        }
    }
}