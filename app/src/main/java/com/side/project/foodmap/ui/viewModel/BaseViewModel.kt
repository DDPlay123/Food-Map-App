package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.api.restaurant.DistanceSearchRes
import com.side.project.foodmap.data.repo.DataStoreRepo
import com.side.project.foodmap.data.repo.DistanceSearchRepo
import com.side.project.foodmap.util.AES
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException

abstract class BaseViewModel : ViewModel(), KoinComponent {
    private val dataStoreRepo: DataStoreRepo by inject()
    private val distanceSearchRepo: DistanceSearchRepo by inject()

    /**
     * 資料流
     */
    private val _userAccount = MutableStateFlow("")
    val userAccount: StateFlow<String>
        get() = _userAccount

    private val _userPassword = MutableStateFlow("")
    val userPassword: StateFlow<String>
        get() = _userPassword

    private val _accessKey = MutableStateFlow("")
    val accessKey: StateFlow<String>
        get() = _accessKey

    private val _deviceId = MutableStateFlow("")
    val deviceId: StateFlow<String>
        get() = _deviceId

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

    private val _getDistanceSearch = MutableStateFlow<Resource<DistanceSearchRes>>(Resource.Loading())
    val getDistanceSearch
        get() = _getDistanceSearch.asStateFlow()

    /**
     * Datastore Preference Repo
     */
    fun putUserAccount(account: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putAccount(account)
        getUserAccountFromDataStore()
    }

    fun getUserAccountFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        _userAccount.emit(dataStoreRepo.getAccount())
    }

    fun putUserPassword(password: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putPassword(password)
        getUserPasswordFromDataStore()
    }

    fun getUserPasswordFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        val decrypt = AES.decrypt("MMSLAB", dataStoreRepo.getPassword())
        _userPassword.emit(decrypt)
    }

    fun putAccessKey(accessKey: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putAccessKey(accessKey)
        getAccessKeyFromDataStore()
    }

    fun getAccessKeyFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        _accessKey.emit(dataStoreRepo.getAccessKey())
    }

    fun putDeviceId(deviceId: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putDeviceId(deviceId)
        getDeviceId()
    }

    fun getDeviceId() = viewModelScope.launch(Dispatchers.IO) {
        _deviceId.emit(dataStoreRepo.getDeviceId())
    }

    fun putUserUID(UID: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putUserUID(UID)
        getUserUIDFromDataStore()
    }

    fun getUserUIDFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        _userUID.emit(dataStoreRepo.getUserUID())
    }

    fun putUserRegion(region: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putUserRegion(region)
        getUserRegionFromDataStore()
    }

    fun getUserRegionFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        _userRegion.emit(dataStoreRepo.getUserRegion())
    }

    fun putUserName(name: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putUserName(name)
        getUserNameFromDataStore()
    }

    fun getUserNameFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        _userName.emit(dataStoreRepo.getUserName())
    }

    fun putUserPicture(picture: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putUserPicture(picture)
        getUserPictureFromDataStore()
    }

    fun getUserPictureFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        _userPicture.emit(dataStoreRepo.getUserPicture())
    }

    fun putUserIsLogin(isLogin: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepo.putUserIsLogin(isLogin)
        getUserIsLoginFromDataStore()
    }

    fun getUserIsLoginFromDataStore() = viewModelScope.launch(Dispatchers.IO) {
        _userIsLogin.postValue(dataStoreRepo.getUserIsLogin())
    }

    /**
     * Database Repo
     */
    fun getDistanceSearchData() {
        viewModelScope.launch { _getDistanceSearch.emit(Resource.Loading()) }
        try {
            viewModelScope.launch(Dispatchers.Default) {
                distanceSearchRepo.getData().let {
                    _getDistanceSearch.emit(Resource.Success(it))
                }
            }
        } catch (e: IOException) {
            viewModelScope.launch {
                _getDistanceSearch.emit(Resource.Error("ERROR"))
            }
        }
    }

    suspend fun insertDistanceSearchData(distanceSearchRes: DistanceSearchRes) {
        deleteDistanceSearchData()
        distanceSearchRepo.insertData(distanceSearchRes)
        getDistanceSearchData()
    }

    suspend fun updateDistanceSearchData(distanceSearchRes: DistanceSearchRes) {
        deleteDistanceSearchData()
        distanceSearchRepo.updateData(distanceSearchRes)
        getDistanceSearchData()
    }

    suspend fun deleteDistanceSearchData() =
        distanceSearchRepo.deleteData()

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

    fun clearPublicData() = viewModelScope.launch(Dispatchers.Default) {
        dataStoreRepo.clearPublicData()
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