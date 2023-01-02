package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.distanceSearch.DistanceSearchDao
import com.side.project.foodmap.data.remote.restaurant.DistanceSearchRes
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DistanceSearchRepo {
    fun getData(): DistanceSearchRes
    suspend fun insertData(distanceSearchRes: DistanceSearchRes)
    suspend fun deleteData()
}

class DistanceSearchRepoImpl : DistanceSearchRepo, KoinComponent {
    private val distanceSearchDao: DistanceSearchDao by inject()

    override fun getData(): DistanceSearchRes =
        distanceSearchDao.getData()

    override suspend fun insertData(distanceSearchRes: DistanceSearchRes) =
        distanceSearchDao.insertData(distanceSearchRes)

    override suspend fun deleteData() =
        distanceSearchDao.deleteData()
}