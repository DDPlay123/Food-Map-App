package com.side.project.foodmap.di

import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.side.project.foodmap.data.local.distanceSearch.DistanceSearchDb
import com.side.project.foodmap.data.local.drawCard.DrawCardDb
import com.side.project.foodmap.data.repo.*
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.util.NetworkConnection
import com.side.project.foodmap.ui.viewModel.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val otherModule = module {
    single { AnimManager(androidContext()) }
    single { NetworkConnection(androidContext()) }
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
}

val daoModel = module {
    single { get<DistanceSearchDb>().distanceSearchDao() }
    single { get<DrawCardDb>().drawCardDao() }
}

val repoModule = module {
    single<DataStoreRepo> { DataStoreRepoImpl(androidContext()) }
    single<DistanceSearchRepo> { DistanceSearchRepoImpl() }
    single<DrawCardRepo> { DrawCardRepoImpl() }
}

val viewModel = module {
    viewModel { LoginViewModel() }
    viewModel { MainViewModel() }
    viewModel { DetailViewModel() }
    viewModel { ListViewModel() }
}