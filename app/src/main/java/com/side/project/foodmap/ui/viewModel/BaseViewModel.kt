package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.repo.DataStoreRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {
    private val dataStoreRepo: DataStoreRepo by inject()

    /**
     * 資料流
     */
    private val _userUID = MutableLiveData<String>()
    val userUID: LiveData<String>
        get() = _userUID

    private val _userTdxToken = MutableLiveData<String>()
    val userTdxToken: LiveData<String>
        get() = _userTdxToken

    private val _userTdxTokenUpdate = MutableLiveData<String>()
    val userTdxTokenUpdate: LiveData<String>
        get() = _userTdxTokenUpdate

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName

    private val _userPicture = MutableLiveData<String>()
    val userPicture: LiveData<String>
        get() = _userPicture

    private val _userIsLogin = MutableLiveData<Boolean>()
    val userIsLogin: LiveData<Boolean>
        get() = _userIsLogin

    /**
     * Datastore Preference Repo
     */
    fun putUserUID(UID: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserUID(UID)
    }

    fun getUserUID() = viewModelScope.launch(Dispatchers.Default) {
        _userUID.postValue(dataStoreRepo.getUserUID())
    }

    fun putUserTdxToken(token: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putTdxToken(token)
    }

    fun getUserTdxToken() = viewModelScope.launch(Dispatchers.Default) {
        _userTdxToken.postValue(dataStoreRepo.getTdxToken())
    }

    fun putUserTdxTokenUpdate(date: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putTdxTokenUpdate(date)
    }

    fun getUserTdxTokenUpdate() = viewModelScope.launch(Dispatchers.Default) {
        _userTdxTokenUpdate.postValue(dataStoreRepo.getTdxTokenUpdate())
    }

    fun putUserName(name: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserName(name)
    }

    fun getUserName() = viewModelScope.launch(Dispatchers.Default) {
        _userName.postValue(dataStoreRepo.getUserName())
    }

    fun putUserPicture(picture: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserPicture(picture)
    }

    fun getUserPicture() = viewModelScope.launch(Dispatchers.Default) {
        _userPicture.postValue(dataStoreRepo.getUserPicture())
    }

    fun putUserIsLogin(isLogin: Boolean) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserIsLogin(isLogin)
    }

    fun getUserIsLogin() = viewModelScope.launch(Dispatchers.Default) {
        _userIsLogin.postValue(dataStoreRepo.getUserIsLogin())
    }

    fun clearData() = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.clearData()
    }
}