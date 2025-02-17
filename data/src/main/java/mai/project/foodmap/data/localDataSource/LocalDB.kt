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
    version = 5,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
internal abstract class LocalDB : RoomDatabase() {
    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 建立新表 MY_BLOCKED_RESTAURANT_new（不含 distance, isFavorite 欄位）
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `MY_BLOCKED_RESTAURANT_new` (
                        `index` TEXT NOT NULL,
                        `placeCount` INTEGER NOT NULL,
                        `placeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `photos` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `ratingStar` REAL NOT NULL,
                        `ratingTotal` INTEGER NOT NULL,
                        `lat` REAL NOT NULL,
                        `lng` REAL NOT NULL,
                        PRIMARY KEY(`index`)
                    )
                    """.trimIndent()
                )
                // 複製舊表資料
                db.execSQL(
                    """
                    INSERT INTO MY_BLOCKED_RESTAURANT_new (`index`, placeCount, placeId, name, photos, 
                    address, ratingStar, ratingTotal, lat, lng)
                    SELECT `index`, placeCount, placeId, name, photos, address, ratingStar, ratingTotal, 
                    lat, lng FROM MY_BLOCKED_RESTAURANT
                    """.trimIndent()
                )
                // 刪除舊的 MY_BLOCKED_RESTAURANT 表
                db.execSQL("DROP TABLE MY_BLOCKED_RESTAURANT")
                // 將新表重新命名為 MY_BLOCKED_RESTAURANT
                db.execSQL("ALTER TABLE MY_BLOCKED_RESTAURANT_new RENAME TO MY_BLOCKED_RESTAURANT")
            }
        }
    }

    abstract fun mySavedPlaceDao(): MySavedPlaceDao

    abstract fun myFavoriteDao(): MyFavoriteDao

    abstract fun myBlacklistDao(): MyBlacklistDao
}