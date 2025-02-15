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
    version = 4,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
internal abstract class LocalDB : RoomDatabase() {
    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 建立新表 MY_FAVORITE_RESTAURANT_new（含 isFavorite 欄位）
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `MY_FAVORITE_RESTAURANT_new` (
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
                        `isFavorite` INTEGER NOT NULL,
                        PRIMARY KEY(`index`)
                    )
                    """.trimIndent()
                )
                // 複製舊表資料（設定 isFavorite 欄位）
                db.execSQL(
                    """
                    INSERT INTO MY_FAVORITE_RESTAURANT_new (`index`, placeId, photos, name, address, workDay, dineIn,
                    takeout, delivery, website, phone, ratingStar, ratingTotal, priceLevel, lat, lng, shareLink, isFavorite)
                    SELECT `index`, placeId, photos, name, address, workDay, dineIn, takeout, delivery, website,
                    phone, ratingStar, ratingTotal, priceLevel, lat, lng, shareLink, 1 FROM MY_FAVORITE_RESTAURANT
                    """.trimIndent()
                )
                // 刪除舊的 MY_FAVORITE_RESTAURANT 表
                db.execSQL("DROP TABLE MY_FAVORITE_RESTAURANT")
                // 將新表重新命名為 MY_FAVORITE_RESTAURANT
                db.execSQL("ALTER TABLE MY_FAVORITE_RESTAURANT_new RENAME TO MY_FAVORITE_RESTAURANT")
            }
        }
    }

    abstract fun mySavedPlaceDao(): MySavedPlaceDao

    abstract fun myFavoriteDao(): MyFavoriteDao

    abstract fun myBlacklistDao(): MyBlacklistDao
}