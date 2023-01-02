package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.getFavorite.GetFavoriteDao
import com.side.project.foodmap.data.remote.FavoriteList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetFavoriteRepo {
    fun getData(): List<FavoriteList>
    fun insertData(favoriteList: FavoriteList)
    fun insertAllData(favoriteLists: List<FavoriteList>)
    fun deleteData(favoriteList: FavoriteList)
    fun deleteAllData()
}

class GetFavoriteRepoImpl : GetFavoriteRepo, KoinComponent {
    private val getFavoriteDao: GetFavoriteDao by inject()

    override fun getData(): List<FavoriteList> =
        getFavoriteDao.getData()

    override fun insertData(favoriteList: FavoriteList) = runBlocking(Dispatchers.IO) {
        getFavoriteDao.insertData(favoriteList)
    }

    override fun insertAllData(favoriteLists: List<FavoriteList>) = runBlocking(Dispatchers.IO) {
        getFavoriteDao.insertAllData(favoriteLists)
    }

    override fun deleteData(favoriteList: FavoriteList) = runBlocking(Dispatchers.IO) {
        getFavoriteDao.deleteData(favoriteList)
    }

    override fun deleteAllData() = runBlocking(Dispatchers.IO) {
        getFavoriteDao.deleteAllData()
    }
}