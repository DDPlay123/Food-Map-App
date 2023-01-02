package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.getBlackList.GetBlackListDao
import com.side.project.foodmap.data.remote.PlaceList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetBlackListRepo {
    fun getData(): List<PlaceList>
    fun insertData(placeList: PlaceList)
    fun insertAllData(placeLists: List<PlaceList>)
    fun deleteData(placeList: PlaceList)
    fun deleteAllData()
}

class GetBlackListRepoImpl : GetBlackListRepo, KoinComponent {
    private val getBlackListDao: GetBlackListDao by inject()

    override fun getData(): List<PlaceList> =
        getBlackListDao.getData()

    override fun insertData(placeList: PlaceList) = runBlocking(Dispatchers.IO) {
        getBlackListDao.insertData(placeList)
    }

    override fun insertAllData(placeLists: List<PlaceList>) = runBlocking(Dispatchers.IO) {
        getBlackListDao.insertAllData(placeLists)
    }

    override fun deleteData(placeList: PlaceList) = runBlocking(Dispatchers.IO) {
        getBlackListDao.deleteData(placeList)
    }

    override fun deleteAllData() = runBlocking(Dispatchers.IO) {
        getBlackListDao.deleteAllData()
    }
}