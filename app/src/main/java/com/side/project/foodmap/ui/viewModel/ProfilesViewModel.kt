package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.api.user.*
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilesViewModel : BaseViewModel() {

    /**
     * 資料流
     */
    private val _logoutState = MutableStateFlow<Resource<LogoutRes>>(Resource.Unspecified())
    val logoutState
        get() = _logoutState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<Resource<DeleteAccountRes>>(Resource.Unspecified())
    val deleteAccountState
        get() = _deleteAccountState.asStateFlow()

    private val _setUserImageState = MutableStateFlow<Resource<SetUserImageRes>>(Resource.Unspecified())
    val setUserImageState
        get() = _setUserImageState.asStateFlow()

    /**
     * 可呼叫方法
     */
    fun logout() {
        val logoutReq = LogoutReq(
            userId = userUID.value,
            accessKey = accessKey.value,
            deviceId = deviceId.value
        )
        viewModelScope.launch { _logoutState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiUserLogout(logoutReq).enqueue(object : Callback<LogoutRes> {
            override fun onResponse(call: Call<LogoutRes>, response: Response<LogoutRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _logoutState.value = Resource.Success(it)
                                putUserIsLogin(false)
                            }
                            else -> _logoutState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<LogoutRes>, t: Throwable) {
                viewModelScope.launch {
                    _logoutState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun deleteAccount() {
        val deleteAccountReq = DeleteAccountReq(
            userId = userUID.value,
            accessKey = accessKey.value
        )
        viewModelScope.launch { _deleteAccountState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiDeleteAccount(deleteAccountReq).enqueue(object : Callback<DeleteAccountRes> {
            override fun onResponse(call: Call<DeleteAccountRes>, response: Response<DeleteAccountRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _deleteAccountState.value = Resource.Success(it)
                                clearData()
                                clearPublicData()
                            }
                            else -> _deleteAccountState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DeleteAccountRes>, t: Throwable) {
                viewModelScope.launch {
                    _deleteAccountState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }

    fun setUserImage(userImage: String) {
        val setUserImageReq = SetUserImageReq(
            userId = userUID.value,
            accessKey = accessKey.value,
            userImage = userImage
        )
        viewModelScope.launch { _setUserImageState.emit(Resource.Loading()) }
        ApiClient.getAPI.apiSetUserImage(setUserImageReq).enqueue(object : Callback<SetUserImageRes> {
            override fun onResponse(call: Call<SetUserImageRes>, response: Response<SetUserImageRes>) {
                viewModelScope.launch {
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                _setUserImageState.value = Resource.Success(it)
                                putUserPicture(userImage)
                            }
                            else -> _setUserImageState.value = Resource.Error(it.errMsg.toString())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SetUserImageRes>, t: Throwable) {
                viewModelScope.launch {
                    _setUserImageState.value = Resource.Error(t.message.toString())
                }
            }
        })
    }
}