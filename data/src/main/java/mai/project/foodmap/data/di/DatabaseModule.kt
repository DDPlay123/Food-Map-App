package mai.project.foodmap.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mai.project.foodmap.data.localDataSource.LocalDB
import mai.project.foodmap.data.localDataSource.LocalDB.Companion.MIGRATION_3_4
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Singleton
    @Provides
    fun provideLocalDB(
        @ApplicationContext
        context: Context
    ) = Room.databaseBuilder(
        context,
        LocalDB::class.java,
        "local_db"
    ).addMigrations(MIGRATION_3_4)
        .build()

    @Singleton
    @Provides
    fun provideMySavedPlaceDao(database: LocalDB) = database.mySavedPlaceDao()

    @Singleton
    @Provides
    fun provideMyFavoriteDao(database: LocalDB) = database.myFavoriteDao()

    @Singleton
    @Provides
    fun provideMyBlacklistDao(database: LocalDB) = database.myBlacklistDao()
}