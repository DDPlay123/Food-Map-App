package com.side.project.foodmap.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.side.project.foodmap.data.repo.DataStoreRepo
import com.side.project.foodmap.data.repo.DataStoreRepoImpl
import com.side.project.foodmap.ui.viewModel.LoginViewModel
import com.side.project.foodmap.util.AnimManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val managerModule = module {
    single { AnimManager(androidContext()) }
}

val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { Firebase.firestore }
}

val repoModule = module {
    single<DataStoreRepo> { DataStoreRepoImpl(androidContext()) }
}

val viewModel = module {
    viewModel { LoginViewModel() }
}