package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.getPlaceList.GetPlaceListDao
import com.side.project.foodmap.data.remote.MyPlaceList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GetPlaceListRepo {
    fun getData(): List<MyPlaceList>
    fun insertData(placeList: MyPlaceList)
    fun insertAllData(placeLists: List<MyPlaceList>)
    fun deleteData(placeList: MyPlaceList)
    fun deleteAllData()
}

class GetPlaceListRepoImpl : GetPlaceListRepo, KoinComponent {
    private val getPlaceListDao: GetPlaceListDao by inject()

    override fun getData(): List<MyPlaceList> =
        getPlaceListDao.getData()

    override fun insertData(placeList: MyPlaceList) = runBlocking(Dispatchers.IO) {
        getPlaceListDao.insertData(placeList)
    }

    override fun insertAllData(placeLists: List<MyPlaceList>) = runBlocking(Dispatchers.IO) {
        getPlaceListDao.insertAllData(placeLists)
    }

    override fun deleteData(placeList: MyPlaceList) = runBlocking(Dispatchers.IO) {
        getPlaceListDao.deleteData(placeList)
    }

    override fun deleteAllData() = runBlocking(Dispatchers.IO) {
        getPlaceListDao.deleteAllData()
    }
}