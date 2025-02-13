package mai.project.foodmap.data.localDataSource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import mai.project.foodmap.data.localDataSource.dao.MyBlacklistDao
import mai.project.foodmap.data.localDataSource.dao.MyFavoriteDao
import mai.project.foodmap.data.localDataSource.dao.MySavedPlaceDao
import mai.project.foodmap.data.localDataSource.entities.MyBlacklistEntity
import mai.project.foodmap.data.localDataSource.entities.MyFavoriteEntity
import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity

@Database(
    entities = [
        MySavedPlaceEntity::class,
        MyFavoriteEntity::class,
        MyBlacklistEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
internal abstract class LocalDB : RoomDatabase() {
    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 建立新的 MY_BLOCKED_RESTAURANT 表
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `$MY_BLOCKED_RESTAURANT` (
                        `index` TEXT NOT NULL,
                        `placeCount` INTEGER NOT NULL,
                        `placeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `photos` TEXT NOT NULL,
                        `ratingStar` REAL NOT NULL,
                        `ratingTotal` INTEGER NOT NULL,
                        `address` TEXT NOT NULL,
                        `lat` REAL NOT NULL,
                        `lng` REAL NOT NULL,
                        `distance` REAL NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        PRIMARY KEY(`index`)
                    )
                    """.trimIndent()
                )
            }
        }
    }

    abstract fun mySavedPlaceDao(): MySavedPlaceDao

    abstract fun myFavoriteDao(): MyFavoriteDao

    abstract fun myBlacklistDao(): MyBlacklistDao
}