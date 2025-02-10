package mai.project.foodmap.data.localDataSource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mai.project.foodmap.data.localDataSource.dao.MySavedPlaceDao
import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity

@Database(
    entities = [
        MySavedPlaceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
internal abstract class LocalDB : RoomDatabase() {

    abstract fun mySavedPlaceDao(): MySavedPlaceDao
}