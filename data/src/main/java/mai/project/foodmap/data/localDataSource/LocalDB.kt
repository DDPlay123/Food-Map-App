package mai.project.foodmap.data.localDataSource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import mai.project.foodmap.data.localDataSource.dao.MyFavoriteDao
import mai.project.foodmap.data.localDataSource.dao.MySavedPlaceDao
import mai.project.foodmap.data.localDataSource.entities.MyFavoriteEntity
import mai.project.foodmap.data.localDataSource.entities.MySavedPlaceEntity

@Database(
    entities = [
        MySavedPlaceEntity::class,
        MyFavoriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
internal abstract class LocalDB : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // (1) 針對 MY_SAVED_PLACE 表：
                // 建立新表 MY_SAVED_PLACE_new（不含 status 欄位）
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `MY_SAVED_PLACE_new` (
                        `index` TEXT NOT NULL,
                        `placeCount` INTEGER NOT NULL,
                        `placeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `lat` REAL NOT NULL,
                        `lng` REAL NOT NULL,
                        PRIMARY KEY(`index`)
                    )
                    """.trimIndent()
                )
                // 複製舊表資料（忽略 status 欄位）
                db.execSQL(
                    """
                    INSERT INTO MY_SAVED_PLACE_new (`index`, placeCount, placeId, name, address, lat, lng)
                    SELECT `index`, placeCount, placeId, name, address, lat, lng FROM MY_SAVED_PLACE
                    """.trimIndent()
                )
                // 刪除舊的 MY_SAVED_PLACE 表
                db.execSQL("DROP TABLE MY_SAVED_PLACE")
                // 將新表重新命名為 MY_SAVED_PLACE
                db.execSQL("ALTER TABLE MY_SAVED_PLACE_new RENAME TO MY_SAVED_PLACE")

                // (2) 建立新的 MY_FAVORITE_RESTAURANT 表
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `$MY_FAVORITE_RESTAURANT` (
                        `index` TEXT NOT NULL,
                        `placeId` TEXT NOT NULL,
                        `photos` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `workDay` TEXT NOT NULL,
                        `dineIn` INTEGER NOT NULL,
                        `takeout` INTEGER NOT NULL,
                        `delivery` INTEGER NOT NULL,
                        `website` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `ratingStar` REAL NOT NULL,
                        `ratingTotal` INTEGER NOT NULL,
                        `priceLevel` INTEGER NOT NULL,
                        `lat` REAL NOT NULL,
                        `lng` REAL NOT NULL,
                        `shareLink` TEXT NOT NULL,
                        PRIMARY KEY(`index`)
                    )
                    """.trimIndent()
                )
            }
        }
    }

    abstract fun mySavedPlaceDao(): MySavedPlaceDao

    abstract fun myFavoriteDao(): MyFavoriteDao
}