package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.getFavorite.GetFavoriteDao
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.util.tools.Coroutines
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

    override fun insertData(favoriteList: FavoriteList) {
        Coroutines.io { getFavoriteDao.insertData(favoriteList) }
    }

    override fun insertAllData(favoriteLists: List<FavoriteList>) {
        Coroutines.io { getFavoriteDao.insertAllData(favoriteLists) }
    }

    override fun deleteData(favoriteList: FavoriteList) {
        Coroutines.io { getFavoriteDao.deleteData(favoriteList) }
    }

    override fun deleteAllData() {
        Coroutines.io { getFavoriteDao.deleteAllData() }
    }
}