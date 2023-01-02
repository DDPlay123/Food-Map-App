package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.drawCard.DrawCardDao
import com.side.project.foodmap.data.remote.restaurant.DrawCardRes
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DrawCardRepo {
    fun getData(): DrawCardRes
    suspend fun insertData(drawCardRes: DrawCardRes)
    suspend fun deleteData()
}

class DrawCardRepoImpl : DrawCardRepo, KoinComponent {
    private val drawCardDao: DrawCardDao by inject()

    override fun getData(): DrawCardRes =
        drawCardDao.getData()

    override suspend fun insertData(drawCardRes: DrawCardRes) =
        drawCardDao.insertData(drawCardRes)

    override suspend fun deleteData() =
        drawCardDao.deleteData()
}