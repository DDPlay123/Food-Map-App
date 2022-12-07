package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.data.remote.api.user.*
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.*
import com.side.project.foodmap.util.tools.AES
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : BaseViewModel() {

    /**
     * 資料流
     */
    private val _loginState = MutableSharedFlow<Resource<LoginRes>>()
    val loginState
        get() = _loginState.asSharedFlow()

    private val _registerState = MutableSharedFlow<Resource<RegisterRes>>()
    val registerState
        get() = _registerState.asSharedFlow()

    private val _validation = Channel<RegisterLoginFieldsState>()
    val validation
        get() = _validation.receiveAsFlow()

    private val _getUserImageState = MutableLiveData<Resource<GetUserImageRes>>()
    val getUserImageState: LiveData<Resource<GetUserImageRes>>
        get() = _getUserImageState

    /**
     * 可呼叫方法
     */
    fun login(account: String, password: String, deviceId: String) {
        val loginReq = LoginReq(
            username = account,
            password = AES.encrypt("MMSLAB", password),
            deviceId = deviceId
        )
        if (checkValidation(account, password)) {
            viewModelScope.launch { _loginState.emit(Resource.Loading()) }
            ApiClient.getAPI.apiUserLogin(loginReq).enqueue(object : Callback<LoginRes> {
                override fun onResponse(call: Call<LoginRes>, response: Response<LoginRes>) {
                    viewModelScope.launch {
                        response.body()?.let {
                            when (it.status) {
                                0 -> {
                                    _loginState.emit(Resource.Success(it, loginReq.password))
                                    setUserInfo(it, loginReq.username)
                                }
                                3 -> _loginState.emit(Resource.Success(it))
                                else -> _loginState.emit(Resource.Error(it.errMsg.toString()))
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<LoginRes>, t: Throwable) {
                    viewModelScope.launch {
                        _loginState.emit(Resource.Error(t.message.toString()))
                    }
                }
            })
        } else {
            viewModelScope.launch {
                _validation.send(
                    RegisterLoginFieldsState(
                        account = Method.validateAccount(account),
                        password = Method.validatePassword(password)
                    )
                )
            }
        }
    }

    fun register(account: String, password: String, deviceId: String) {
        if (checkValidation(account, password)) {
            val registerReq = RegisterReq(
                username = account,
                password = AES.encrypt("MMSLAB", password),
                deviceId = deviceId
            )
            viewModelScope.launch { _registerState.emit(Resource.Loading()) }
            ApiClient.getAPI.apiUserRegister(registerReq).enqueue(object : Callback<RegisterRes> {
                override fun onResponse(call: Call<RegisterRes>, response: Response<RegisterRes>) {
                    viewModelScope.launch {
                        response.body()?.let {
                            when (it.status) {
                                0 -> _registerState.emit(Resource.Success(it))
                                else -> _registerState.emit(Resource.Error(it.errMsg.toString()))
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<RegisterRes>, t: Throwable) {
                    viewModelScope.launch {
                        _registerState.emit(Resource.Error(t.message.toString()))
                    }
                }
            })
        } else {
            viewModelScope.launch {
                _validation.send(
                    RegisterLoginFieldsState(
                        account = Method.validateAccount(account),
                        password = Method.validatePassword(password)
                    )
                )
            }
        }
    }

    private fun getUserImage(loginRes: LoginRes) {
        val getUserImageReq = GetUserImageReq(
            accessKey = loginRes.result?.accessKey ?: accessKey.value,
            userId = loginRes.result?.userId ?: userUID.value,
        )
        viewModelScope.launch { _getUserImageState.postValue(Resource.Loading()) }
        ApiClient.getAPI.apiGetUserImage(getUserImageReq)
            .enqueue(object : Callback<GetUserImageRes> {
                override fun onResponse(
                    call: Call<GetUserImageRes>,
                    response: Response<GetUserImageRes>
                ) {
                    viewModelScope.launch {
                        response.body()?.let {
                            when (it.status) {
                                0 -> _getUserImageState.postValue(Resource.Success(it))
                                else -> _getUserImageState.value = Resource.Error(it.errMsg.toString())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<GetUserImageRes>, t: Throwable) {
                    viewModelScope.launch {
                        _getUserImageState.value = Resource.Error(t.message.toString())
                    }
                }
            })
    }

    /**
     * 驗證輸入
     */
    private fun checkValidation(account: String, password: String): Boolean {
        val validAccount = Method.validateAccount(account)
        val validPassword = Method.validatePassword(password)
        return validAccount is RegisterLoginValidation.Success && validPassword is RegisterLoginValidation.Success
    }

    /**
     * 儲存資料到 Datastore
     */
    private fun setUserInfo(loginRes: LoginRes, username: String) {
        loginRes.result?.let {
            putUserUID(it.userId)
            putAccessKey(it.accessKey)
            putUserName(username)
            putUserIsLogin(true)
            getUserImage(loginRes)
        }
    }
}