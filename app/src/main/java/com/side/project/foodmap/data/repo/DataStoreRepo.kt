package com.side.project.foodmap.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.side.project.foodmap.R
import com.side.project.foodmap.util.Constants.USERS_PREFERENCE
import com.side.project.foodmap.util.Constants.USER_ACCESS_KEY
import com.side.project.foodmap.util.Constants.USER_IS_LOGIN
import com.side.project.foodmap.util.Constants.USER_NAME
import com.side.project.foodmap.util.Constants.USER_PICTURE
import com.side.project.foodmap.util.Constants.USER_REGION
import com.side.project.foodmap.util.Constants.USER_UID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent

interface DataStoreRepo {
    // API AccessKey
    suspend fun putAccessKey(accessKey: String)
    suspend fun getAccessKey(): String
    // 使用者 ID
    suspend fun putUserUID(UID: String)
    suspend fun getUserUID(): String
    // 使用者區域設定
    suspend fun putUserRegion(region: String)
    suspend fun getUserRegion(): String
    // 使用者名稱
    suspend fun putUserName(name: String)
    suspend fun getUserName(): String
    // 使用者圖片
    suspend fun putUserPicture(picture: String)
    suspend fun getUserPicture(): String
    // 使用者是否登入
    suspend fun putUserIsLogin(isLogin: Boolean)
    suspend fun getUserIsLogin(): Boolean
//    suspend fun putTdxToken(token: String)
//    suspend fun getTdxToken(): String
//    suspend fun putTdxTokenUpdate(date: String)
//    suspend fun getTdxTokenUpdate(): String
    suspend fun clearData()
}

class DataStoreRepoImpl(private val context: Context) : DataStoreRepo, KoinComponent {
    private val Context.userInfo: DataStore<Preferences> by preferencesDataStore(name = USERS_PREFERENCE)

    override suspend fun putAccessKey(accessKey: String) {
        context.userInfo.edit {
            it[stringPreferencesKey(USER_ACCESS_KEY)] = accessKey
        }
    }

    override suspend fun getAccessKey(): String =
        context.userInfo.data.map {
            it[stringPreferencesKey(USER_ACCESS_KEY)] ?: ""
        }.first()

    override suspend fun putUserUID(UID: String) {
        context.userInfo.edit {
            it[stringPreferencesKey(USER_UID)] = UID
        }
    }

    override suspend fun getUserUID(): String =
        context.userInfo.data.map {
            it[stringPreferencesKey(USER_UID)] ?: ""
        }.first()

    override suspend fun putUserRegion(region: String) {
        context.userInfo.edit {
            it[stringPreferencesKey(USER_REGION)] = region
        }
    }

    override suspend fun getUserRegion(): String =
        context.userInfo.data.map {
            it[stringPreferencesKey(USER_REGION)] ?: context.getString(R.string.text_taipei)
        }.first()

    override suspend fun putUserName(name: String) {
        context.userInfo.edit {
            it[stringPreferencesKey(USER_NAME)] = name
        }
    }

    override suspend fun getUserName(): String =
        context.userInfo.data.map {
            it[stringPreferencesKey(USER_NAME)] ?: ""
        }.first()

    override suspend fun putUserPicture(picture: String) {
        context.userInfo.edit {
            it[stringPreferencesKey(USER_PICTURE)] = picture
        }
    }

    override suspend fun getUserPicture(): String =
        context.userInfo.data.map {
            it[stringPreferencesKey(USER_PICTURE)] ?: ""
        }.first()

    override suspend fun putUserIsLogin(isLogin: Boolean) {
        context.userInfo.edit {
            it[booleanPreferencesKey(USER_IS_LOGIN)] = isLogin
        }
    }

    override suspend fun getUserIsLogin(): Boolean =
        context.userInfo.data.map {
            it[booleanPreferencesKey(USER_IS_LOGIN)] ?: false
        }.first()

//    override suspend fun putTdxToken(token: String) {
//        context.userInfo.edit {
//            it[stringPreferencesKey(USER_TDX_TOKEN)] = token
//        }
//    }
//
//    override suspend fun getTdxToken(): String =
//        context.userInfo.data.map {
//            it[stringPreferencesKey(USER_TDX_TOKEN)] ?: "2020/12/31"
//        }.first()
//
//    override suspend fun putTdxTokenUpdate(date: String) {
//        context.userInfo.edit {
//            it[stringPreferencesKey(USER_TDX_TOKEN_UPDATE)] = date
//        }
//    }
//
//    override suspend fun getTdxTokenUpdate(): String =
//        context.userInfo.data.map {
//            it[stringPreferencesKey(USER_TDX_TOKEN_UPDATE)] ?: ""
//        }.first()

    override suspend fun clearData() {
        context.userInfo.edit { it.clear() }
    }
}