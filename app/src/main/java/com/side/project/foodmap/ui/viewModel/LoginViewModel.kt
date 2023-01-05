package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.util.*
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    init {
        getUserAccountFromDataStore()
        getUserPasswordFromDataStore()
    }

    /**
     * 資料流
     */
    private val _validation = Channel<RegisterLoginFieldsState>()
    val validation get() = _validation.receiveAsFlow()

    var loginFlow = userApiRepo.loginFlow

    val registerFlow get() = userApiRepo.registerFlow

    val getUserImageFlow get() = userApiRepo.getUserImageFlow

    /**
     * 可呼叫方法
     */
    fun login(
        username: String,
        password: String,
        deviceId: String
    ) {
        if (checkValidation(username, password))
            userApiRepo.apiUserLogin(username, password, deviceId)
        else
            viewModelScope.launch {
                _validation.send(
                    RegisterLoginFieldsState(
                        username = Method.validateAccount(username),
                        password = Method.validatePassword(password)
                    )
                )
            }
    }

    fun register(
        username: String,
        password: String,
        deviceId: String
    ) {
        if (checkValidation(username, password))
            userApiRepo.apiUserRegister(username, password, deviceId)
        else
            viewModelScope.launch {
                _validation.send(
                    RegisterLoginFieldsState(
                        username = Method.validateAccount(username),
                        password = Method.validatePassword(password)
                    )
                )
            }
    }

    fun getUserImage() =
        userApiRepo.apiGetUserImage()

    /**
     * 驗證輸入
     */
    private fun checkValidation(account: String, password: String): Boolean {
        val validAccount = Method.validateAccount(account)
        val validPassword = Method.validatePassword(password)
        return validAccount is RegisterLoginValidation.Success && validPassword is RegisterLoginValidation.Success
    }
}