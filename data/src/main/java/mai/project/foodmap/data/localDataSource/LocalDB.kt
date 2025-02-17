package mai.project.foodmap.data.localDataSource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import mai.project.foodmap.data.localDataSource.dao.MyBlacklistDao
import mai.project.foodmap.data.localDataSource.dao.MyFavoriteDao
import mai.project.foodmap.data.localDataSource.dao.MySavedPlaceDao
import mai.project.foodmap.data.localDataSource.dao.MySearchDap
import mai.project.foodmap.data.localDataSource.entities.MyBlacklistEntity
import mai.project.foodmap.data.localDataSource.entities.MyFavoriteEntity
import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity
import mai.project.foodmap.data.localDataSource.entities.MySearchEntity

@Database(
    entities = [
        MySavedPlaceEntity::class,
        MyFavoriteEntity::class,
        MyBlacklistEntity::class,
        MySearchEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
internal abstract class LocalDB : RoomDatabase() {
    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 建立新的 MY_SEARCH_RESTAURANT 表
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `$MY_SEARCH_RESTAURANT` (
                        `index` TEXT NOT NULL,
                        `placeCount` INTEGER NOT NULL,
                        `placeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `isSearch` INTEGER NOT NULL,
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

    abstract fun mySearchDao(): MySearchDap
}