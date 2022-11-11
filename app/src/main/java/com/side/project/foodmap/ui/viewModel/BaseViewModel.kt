package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.repo.DataStoreRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {
    private val dataStoreRepo: DataStoreRepo by inject()

    /**
     * 資料流
     */
    private val _accessKey = MutableStateFlow("")
    val accessKey: StateFlow<String>
        get() = _accessKey

    private val _userUID = MutableStateFlow("")
    val userUID: StateFlow<String>
        get() = _userUID

    private val _userRegion = MutableStateFlow("")
    val userRegion: StateFlow<String>
        get() = _userRegion

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String>
        get() = _userName

    private val _userPicture = MutableStateFlow("")
    val userPicture: StateFlow<String>
        get() = _userPicture

    private val _userIsLogin = MutableLiveData<Boolean>()
    val userIsLogin: LiveData<Boolean>
        get() = _userIsLogin

//    private val _userTdxToken = MutableLiveData<String>()
//    val userTdxToken: LiveData<String>
//        get() = _userTdxToken
//
//    private val _userTdxTokenUpdate = MutableLiveData<String>()
//    val userTdxTokenUpdate: LiveData<String>
//        get() = _userTdxTokenUpdate

    /**
     * Datastore Preference Repo
     */
    fun putAccessKey(accessKey: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putAccessKey(accessKey)
        getAccessKeyFromDataStore()
    }

    fun getAccessKeyFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _accessKey.emit(dataStoreRepo.getAccessKey())
    }

    fun putUserUID(UID: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserUID(UID)
        getUserUIDFromDataStore()
    }

    fun getUserUIDFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userUID.emit(dataStoreRepo.getUserUID())
    }

    fun putUserRegion(region: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserRegion(region)
        getUserRegionFromDataStore()
    }

    fun getUserRegionFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userRegion.emit(dataStoreRepo.getUserRegion())
    }

    fun putUserName(name: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserName(name)
        getUserNameFromDataStore()
    }

    fun getUserNameFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userName.emit(dataStoreRepo.getUserName())
    }

    fun putUserPicture(picture: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserPicture(picture)
        getUserPictureFromDataStore()
    }

    fun getUserPictureFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userPicture.emit(dataStoreRepo.getUserPicture())
    }

    fun putUserIsLogin(isLogin: Boolean) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserIsLogin(isLogin)
        getUserIsLoginFromDataStore()
    }

    fun getUserIsLoginFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userIsLogin.postValue(dataStoreRepo.getUserIsLogin())
    }

//    fun putUserTdxToken(token: String) = viewModelScope.launch(Dispatchers.Default) {
//        dataStoreRepo.putTdxToken(token)
//        getUserTdxTokenFromDataStore()
//    }
//
//    fun getUserTdxTokenFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
//        _userTdxToken.postValue(dataStoreRepo.getTdxToken())
//    }
//
//    fun putUserTdxTokenUpdate(date: String) = viewModelScope.launch(Dispatchers.Default) {
//        dataStoreRepo.putTdxTokenUpdate(date)
//        getUserTdxTokenUpdate()
//    }
//
//    fun getUserTdxTokenUpdate() = viewModelScope.launch(Dispatchers.Default) {
//        _userTdxTokenUpdate.postValue(dataStoreRepo.getTdxTokenUpdate())
//    }

    fun clearData() = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.clearData()
    }

    /**
     * 呼叫 API
     */
//    fun updateTdxToken(date: String, tdxTokenReq: TdxTokenReq) {
//        ApiClient.getTdxToken.getToken(tdxTokenReq.grant_type, tdxTokenReq.client_id, tdxTokenReq.client_secret).enqueue(object : Callback<TdxTokenRes> {
//            override fun onResponse(call: Call<TdxTokenRes>, response: Response<TdxTokenRes>) {
//                response.body()?.let {
//                    logE("Get New Token", "Success")
//                    putUserTdxToken("Bearer ${it.access_token}")
//                    putUserTdxTokenUpdate(date)
//                }
//            }
//
//            override fun onFailure(call: Call<TdxTokenRes>, t: Throwable) {
//                logE("Get New Token", "Error:${t.message.toString()}")
//            }
//        })
//    }
}