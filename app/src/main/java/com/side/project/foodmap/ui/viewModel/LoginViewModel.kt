package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.user.LoginReq
import com.side.project.foodmap.data.remote.api.user.LoginRes
import com.side.project.foodmap.data.remote.api.user.RegisterReq
import com.side.project.foodmap.data.remote.api.user.RegisterRes
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.network.ApiClient.getAPI
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.RegisterLoginFieldsState
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : BaseViewModel() {
    init {
        getUserAccountFromDataStore()
        getUserPasswordFromDataStore()
    }

    /**
     * 資料流
     */
    private val _loginState = MutableStateFlow<Resource<LoginRes>>(Resource.Unspecified())
    val loginState
        get() = _loginState.asSharedFlow()

    private val _registerState = MutableStateFlow<Resource<RegisterRes>>(Resource.Unspecified())
    val registerState
        get() = _registerState.asStateFlow()

    private val _validation = Channel<RegisterLoginFieldsState>()
    val validation
        get() = _validation.receiveAsFlow()

    /**
     * 可呼叫方法
     */
    fun login(loginReq: LoginReq) {
        if (checkValidation(loginReq.username, loginReq.password)) {
            viewModelScope.launch { _loginState.emit(Resource.Loading()) }
            getAPI.apiUserLogin(loginReq).enqueue(object : Callback<LoginRes> {
                override fun onResponse(call: Call<LoginRes>, response: Response<LoginRes>) {
                    viewModelScope.launch {
                        response.body()?.let {
                            when (it.status) {
                                0 -> {
                                    _loginState.value = Resource.Success(it)
                                    setUserInfo(it, loginReq.username)
                                }
                                3 -> _loginState.value = Resource.Success(it)
                                else -> _loginState.value = Resource.Error(it.errMsg.toString())
                            }
                            _loginState.value = Resource.Success(it)
                        }
                    }
                }

                override fun onFailure(call: Call<LoginRes>, t: Throwable) {
                    viewModelScope.launch {
                        _loginState.value = Resource.Error(t.message.toString())
                    }
                }
            })
        } else {
            viewModelScope.launch {
                _validation.send(
                    RegisterLoginFieldsState(
                        Method.validateAccount(loginReq.username),
                        Method.validatePassword(loginReq.password)
                    )
                )
            }
        }
    }

    fun register(registerReq: RegisterReq) {
        if (checkValidation(registerReq.username, registerReq.password)) {
            viewModelScope.launch { _registerState.emit(Resource.Loading()) }
            getAPI.apiUserRegister(registerReq).enqueue(object : Callback<RegisterRes> {
                override fun onResponse(call: Call<RegisterRes>, response: Response<RegisterRes>) {
                    viewModelScope.launch {
                        response.body()?.let {
                            when (it.status) {
                                // TODO(註冊)
                            }
                            _registerState.value = Resource.Success(it)
                        }
                    }
                }

                override fun onFailure(call: Call<RegisterRes>, t: Throwable) {
                    viewModelScope.launch {
                        _registerState.value = Resource.Error(t.message.toString())
                    }
                }
            })
        } else {
            viewModelScope.launch {
                _validation.send(
                    RegisterLoginFieldsState(
                        Method.validateAccount(registerReq.username),
                        Method.validatePassword(registerReq.password)
                    )
                )
            }
        }
    }

    /**
     * 驗證輸入
     */
    private fun checkValidation(account: String, password: String): Boolean {
        val validAccount = Method.validateAccount(account)
        val validPassword = Method.validatePassword(password)
        return validAccount is RegisterLoginValidation.Success &&
                validPassword is RegisterLoginValidation.Success
    }

    /**
     * 儲存資料到 Datastore
     */
    private fun setUserInfo(loginRes: LoginRes, username: String) {
        loginRes.result?.let {
            putUserUID(it.userId)
            putUserName(username)
            putUserPicture(it.userIcon)
            putUserIsLogin(true)
        }
    }
}