package com.side.project.foodmap.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.side.project.foodmap.data.local.distanceSearch.DistanceSearchDb
import com.side.project.foodmap.data.local.drawCard.DrawCardDb
import com.side.project.foodmap.data.local.getBlackList.GetBlackListDb
import com.side.project.foodmap.data.local.getFavorite.GetFavoriteDb
import com.side.project.foodmap.data.local.getPlaceList.GetPlaceListDb
import com.side.project.foodmap.data.local.historySearch.HistorySearchDb
import com.side.project.foodmap.data.repo.*
import com.side.project.foodmap.data.repo.api.GeocodeApiRepo
import com.side.project.foodmap.data.repo.api.RestaurantApiRepo
import com.side.project.foodmap.data.repo.api.UserApiRepo
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.DialogManager
import com.side.project.foodmap.util.tools.NetworkConnection
import com.side.project.foodmap.ui.viewModel.*
import com.side.project.foodmap.util.tools.LocationGet
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val otherModule = module {
    single { DialogManager() }
    single { AnimManager(androidContext()) }
    single { NetworkConnection(androidContext()) }
    single { LocationGet(androidContext()) }
}

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { Firebase.firestore }
}


val dbModel = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            DistanceSearchDb::class.java,
            DistanceSearchDb::class.java.simpleName
        ).fallbackToDestructiveMigration()
            .build()
    }
    single {
        Room.databaseBuilder(
            androidApplication(),
            DrawCardDb::class.java,
            DrawCardDb::class.java.simpleName
        ).fallbackToDestructiveMigration()
            .build()
    }
    single {
        Room.databaseBuilder(
            androidApplication(),
            GetFavoriteDb::class.java,
            GetFavoriteDb::class.java.simpleName
        ).fallbackToDestructiveMigration()
            .build()
    }
    single {
        Room.databaseBuilder(
            androidApplication(),
            GetBlackListDb::class.java,
            GetBlackListDb::class.java.simpleName
        ).fallbackToDestructiveMigration()
            .build()
    }
    single {
        Room.databaseBuilder(
            androidApplication(),
            GetPlaceListDb::class.java,
            GetPlaceListDb::class.java.simpleName
        ).fallbackToDestructiveMigration()
            .build()
    }
    single {
        Room.databaseBuilder(
            androidApplication(),
            HistorySearchDb::class.java,
            HistorySearchDb::class.java.simpleName
        ).fallbackToDestructiveMigration()
            .build()
    }
}

val daoModel = module {
    single { get<DistanceSearchDb>().distanceSearchDao() }
    single { get<DrawCardDb>().drawCardDao() }
    single { get<GetFavoriteDb>().getFavoriteDao() }
    single { get<GetBlackListDb>().getBlackListDao() }
    single { get<GetPlaceListDb>().getPlaceListDao() }
    single { get<HistorySearchDb>().getHistorySearchDao() }
}

val repoModule = module {
    factory  { RestaurantApiRepo() }
    factory  { GeocodeApiRepo() }
    factory  { UserApiRepo() }
    single<DataStoreRepo> { DataStoreRepoImpl(androidContext()) }
    single<DistanceSearchRepo> { DistanceSearchRepoImpl() }
    single<DrawCardRepo> { DrawCardRepoImpl() }
    single<GetFavoriteRepo> { GetFavoriteRepoImpl() }
    single<GetBlackListRepo> { GetBlackListRepoImpl() }
    single<GetPlaceListRepo> { GetPlaceListRepoImpl() }
    single<HistorySearchRepo> { HistorySearchRepoImpl() }
}

val viewModel = module {
    viewModel { LoginViewModel() }
    viewModel { MainViewModel() }
    viewModel { DetailViewModel() }
    viewModel { ListViewModel() }
    viewModel { GetLocationViewModel() }
}