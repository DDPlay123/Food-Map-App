package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.tdx.TdxTokenReq
import com.side.project.foodmap.data.remote.tdx.TdxTokenRes
import com.side.project.foodmap.data.repo.DataStoreRepo
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Method.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private val _userRegion = MutableLiveData<String>()
    val userRegion: LiveData<String>
        get() = _userRegion

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
        getUserUIDFromDataStore()
    }

    fun getUserUIDFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userUID.postValue(dataStoreRepo.getUserUID())
    }

    fun putUserTdxToken(token: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putTdxToken(token)
        getUserTdxTokenFromDataStore()
    }

    fun getUserTdxTokenFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userTdxToken.postValue(dataStoreRepo.getTdxToken())
    }

    fun putUserTdxTokenUpdate(date: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putTdxTokenUpdate(date)
        getUserTdxTokenUpdate()
    }

    fun getUserTdxTokenUpdate() = viewModelScope.launch(Dispatchers.Default) {
        _userTdxTokenUpdate.postValue(dataStoreRepo.getTdxTokenUpdate())
    }

    fun putUserRegion(region: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserRegion(region)
        getUserRegionFromDataStore()
    }

    fun getUserRegionFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userRegion.postValue(dataStoreRepo.getUserRegion())
    }

    fun putUserName(name: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserName(name)
        getUserNameFromDataStore()
    }

    fun getUserNameFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userName.postValue(dataStoreRepo.getUserName())
    }

    fun putUserPicture(picture: String) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserPicture(picture)
        getUserPictureFromDataStore()
    }

    fun getUserPictureFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userPicture.postValue(dataStoreRepo.getUserPicture())
    }

    fun putUserIsLogin(isLogin: Boolean) = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.putUserIsLogin(isLogin)
        getUserIsLoginFromDataStore()
    }

    fun getUserIsLoginFromDataStore() = viewModelScope.launch(Dispatchers.Default) {
        _userIsLogin.postValue(dataStoreRepo.getUserIsLogin())
    }

    fun clearData() = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.clearData()
    }

    /**
     * 呼叫 API
     */
    fun updateTdxToken(date: String, tdxTokenReq: TdxTokenReq) {
        ApiClient.getTdxToken.getToken(tdxTokenReq.grant_type, tdxTokenReq.client_id, tdxTokenReq.client_secret).enqueue(object : Callback<TdxTokenRes> {
            override fun onResponse(call: Call<TdxTokenRes>, response: Response<TdxTokenRes>) {
                response.body()?.let {
                    logE("Get New Token", "Success")
                    putUserTdxToken("Bearer ${it.access_token}")
                    putUserTdxTokenUpdate(date)
                }
            }

            override fun onFailure(call: Call<TdxTokenRes>, t: Throwable) {
                logE("Get New Token", "Error:${t.message.toString()}")
            }
        })
    }
}