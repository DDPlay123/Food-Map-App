package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.side.project.foodmap.data.local.User
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class LoginViewModel: BaseViewModel() {
    private val firebaseAuth: FirebaseAuth by inject()
    private val fireStore: FirebaseFirestore by inject()

    /**
     * 資料流
     */
    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val loginState
        get() = _loginState.asSharedFlow()

    /**
     * 可呼叫方法
     */
    fun anonymityLogin(user: User) {
        viewModelScope.launch { _loginState.emit(Resource.Loading()) }
        firebaseAuth.signInAnonymously()
            .addOnSuccessListener { result ->
                viewModelScope.launch {
                    result.user?.let {
                        setUserInfoLocal(it.uid, user)
                    }
                }
            }.addOnFailureListener { e ->
                viewModelScope.launch {
                    _loginState.emit(Resource.Error(e.message.toString()))
                }
            }
    }

    /**
     * 設定個人資料到 Firestore
     */
//    private fun setUserInfoRemote(userUID: String, user: User) {
//        fireStore.collection(USER_COLLECTION)
//            .document(userUID)
//            .set(user)
//            .addOnSuccessListener {
//                viewModelScope.launch {
//                    _loginState.value = Resource.Success(user)
//                    withContext(Dispatchers.Default) {
//                        setUserInfoLocal(userUID, user)
//                    }
//                }
//            }.addOnFailureListener { e ->
//                viewModelScope.launch {
//                    _loginState.value = Resource.Error(e.message.toString())
//                }
//            }
//    }

    private fun setUserInfoLocal(userUID: String, user: User) {
        putUserUID(userUID)
        putUserName(user.name)
        putUserPicture(user.imagePath)
        putUserIsLogin(true)
    }
}