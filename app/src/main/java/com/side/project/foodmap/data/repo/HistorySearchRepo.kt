package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.historySearch.HistorySearchDao
import com.side.project.foodmap.data.remote.api.HistorySearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface HistorySearchRepo {
    fun getData(): List<HistorySearch>
    fun insertData(historySearch: HistorySearch)
    fun deleteData(historySearch: HistorySearch)
    fun deleteAllData()
}

class HistorySearchRepoImpl : HistorySearchRepo, KoinComponent {
    private val historySearchDao: HistorySearchDao by inject()

    override fun getData(): List<HistorySearch> = runBlocking(Dispatchers.IO) {
        historySearchDao.getData()
    }

    override fun insertData(historySearch: HistorySearch) = runBlocking(Dispatchers.IO) {
        historySearchDao.insertData(historySearch)
    }

    override fun deleteData(historySearch: HistorySearch) = runBlocking(Dispatchers.IO) {
        historySearchDao.deleteData(historySearch)
    }

    override fun deleteAllData() = runBlocking(Dispatchers.IO) {
        historySearchDao.deleteAllData()
    }
}