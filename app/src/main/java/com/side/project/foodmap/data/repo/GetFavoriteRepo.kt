package com.side.project.foodmap.data.repo

import androidx.lifecycle.LiveData
import com.side.project.foodmap.data.local.getFavorite.GetFavoriteDao
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetFavoriteRepo {
    fun getData(): LiveData<List<FavoriteList>>
    fun insertData(favoriteList: FavoriteList)
    fun deleteData(favoriteList: FavoriteList)
    fun deleteAllData()
}

class GetFavoriteRepoImpl : GetFavoriteRepo, KoinComponent {
    private val getFavoriteDao: GetFavoriteDao by inject()

    override fun getData(): LiveData<List<FavoriteList>> =
        getFavoriteDao.getData()

    override fun insertData(favoriteList: FavoriteList) {
        Method.logE("Favorite", "Insert")
        Coroutines.io { getFavoriteDao.insertData(favoriteList) }
    }

    override fun deleteData(favoriteList: FavoriteList) {
        Method.logE("Favorite", "Delete")
        Coroutines.io { getFavoriteDao.deleteData(favoriteList) }
    }

    override fun deleteAllData() {
        Coroutines.io { getFavoriteDao.deleteAllData() }
    }
}