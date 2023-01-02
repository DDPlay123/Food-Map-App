package com.side.project.foodmap.data.repo

import com.side.project.foodmap.data.local.historySearch.HistorySearchDao
import com.side.project.foodmap.data.remote.AutoComplete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface HistorySearchRepo {
    fun getData(): List<AutoComplete>
    fun insertData(historySearch: AutoComplete)
    fun deleteData(historySearch: AutoComplete)
    fun deleteAllData()
}

class HistorySearchRepoImpl : HistorySearchRepo, KoinComponent {
    private val historySearchDao: HistorySearchDao by inject()

    override fun getData(): List<AutoComplete> = runBlocking(Dispatchers.IO) {
        historySearchDao.getData()
    }

    override fun insertData(historySearch: AutoComplete) = runBlocking(Dispatchers.IO) {
        historySearchDao.insertData(historySearch)
    }

    override fun deleteData(historySearch: AutoComplete) = runBlocking(Dispatchers.IO) {
        historySearchDao.deleteData(historySearch)
    }

    override fun deleteAllData() = runBlocking(Dispatchers.IO) {
        historySearchDao.deleteAllData()
    }
}