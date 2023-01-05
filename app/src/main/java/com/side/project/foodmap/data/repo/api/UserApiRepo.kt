package com.side.project.foodmap.data.repo.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.side.project.foodmap.data.remote.FavoriteList
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.MyPlaceList
import com.side.project.foodmap.data.remote.PlaceList
import com.side.project.foodmap.data.remote.user.*
import com.side.project.foodmap.data.remote.user.blackList.*
import com.side.project.foodmap.data.remote.user.favoriteList.*
import com.side.project.foodmap.data.remote.user.placeList.*
import com.side.project.foodmap.data.repo.*
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.AES
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserApiRepo : KoinComponent {
    private val dataStoreRepo: DataStoreRepo by inject()
    private val distanceSearchRepo: DistanceSearchRepo by inject()
    private val drawCardRepo: DrawCardRepo by inject()
    private val getFavoriteRepo: GetFavoriteRepo by inject()
    private val getBlackListRepo: GetBlackListRepo by inject()
    private val getPlaceListRepo: GetPlaceListRepo by inject()
    private val historySearchRepo: HistorySearchRepo by inject()

    private val mLoginFlow = MutableSharedFlow<Resource<LoginRes>>()
    val loginFlow
        get() = mLoginFlow.asSharedFlow()
    private val mLogoutFlow = MutableSharedFlow<Resource<LogoutRes>>()
    val logoutFlow
        get() = mLogoutFlow.asSharedFlow()
    private val mRegisterFlow = MutableSharedFlow<Resource<RegisterRes>>()
    val registerFlow
        get() = mRegisterFlow.asSharedFlow()
    private val mDeleteAccountFlow = MutableSharedFlow<Resource<DeleteAccountRes>>()
    val deleteAccountFlow
        get() = mDeleteAccountFlow.asSharedFlow()

    private val mPutFcmTokenFlow = MutableSharedFlow<Resource<AddFcmTokenRes>>()
    val putFcmTokenFlow
        get() = mPutFcmTokenFlow.asSharedFlow()
    private val mSetUserImageFlow = MutableSharedFlow<Resource<SetUserImageRes>>()
    val setUserImageFlow
        get() = mSetUserImageFlow.asSharedFlow()
    private val mGetUserImageFlow = MutableSharedFlow<Resource<GetUserImageRes>>()
    val getUserImageFlow
        get() = mGetUserImageFlow.asSharedFlow()
    private val mSetUserPasswordFlow = MutableSharedFlow<Resource<SetPasswordRes>>()
    val setUserPasswordFlow
        get() = mSetUserPasswordFlow.asSharedFlow()

    // 最愛清單
    private val mPushFavoriteListFlow = MutableSharedFlow<Resource<PushFavoriteRes>>()
    val pushFavoriteListFlow
        get() = mPushFavoriteListFlow.asSharedFlow()
    private val mPullFavoriteListFlow = MutableSharedFlow<Resource<PullFavoriteRes>>()
    val pullFavoriteListFlow
        get() = mPullFavoriteListFlow.asSharedFlow()
    private val mGetFavoriteListData = MutableLiveData<Resource<GetFavoriteRes>>()
    private val getFavoriteListData: LiveData<Resource<GetFavoriteRes>>
        get() = mGetFavoriteListData
    private val mGetSyncFavoriteListFlow = MutableSharedFlow<List<FavoriteList>>()
    val getSyncFavoriteListFlow
        get() = mGetSyncFavoriteListFlow.asSharedFlow()

    // 黑名單
    private val mPushBlackListFlow = MutableSharedFlow<Resource<PushBlackListRes>>()
    val pushBlackListFlow
        get() = mPushBlackListFlow.asSharedFlow()
    private val mPullBlackListFlow = MutableSharedFlow<Resource<PullBlackListRes>>()
    val pullBlackListFlow
        get() = mPullBlackListFlow.asSharedFlow()
    private val mGetBlackListData = MutableLiveData<Resource<GetBlackListRes>>()
    private val getBlackListData: LiveData<Resource<GetBlackListRes>>
        get() = mGetBlackListData
    private val mGetSyncBlackListFlow = MutableSharedFlow<List<PlaceList>>()
    val getSyncBlackListFlow
        get() = mGetSyncBlackListFlow.asSharedFlow()

    // 地點儲存
    private val mPushPlaceListFlow = MutableSharedFlow<Resource<PushPlaceListRes>>()
    val pushPlaceListFlow
        get() = mPushPlaceListFlow.asSharedFlow()
    private val mPullPlaceListFlow = MutableSharedFlow<Resource<PullPlaceListRes>>()
    val pullPlaceListFlow
        get() = mPullPlaceListFlow.asSharedFlow()
    private val mGetPlaceListData = MutableLiveData<Resource<GetPlaceListRes>>()
    private val getPlaceListData: LiveData<Resource<GetPlaceListRes>>
        get() = mGetPlaceListData
    private val mGetSyncPlaceListFlow = MutableSharedFlow<List<MyPlaceList>>()
    val getSyncPlaceListFlow
        get() = mGetSyncPlaceListFlow.asSharedFlow()

    fun apiUserLogin(
        username: String,
        password: String,
        deviceId: String
    ) {
        Coroutines.io {
            LoginReq(
                username = username,
                password = AES.encrypt(Constants.MMSLAB, password),
                deviceId = deviceId
            ).apply {
                mLoginFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiUserLogin(this).enqueue(object : Callback<LoginRes> {
                    override fun onResponse(call: Call<LoginRes>, response: Response<LoginRes>) {
                        Method.logE(LoginRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        dataStoreRepo.run {
                                            putAccessKey(it.result.accessKey)
                                            putUserUID(it.result.userId)
                                            putDeviceId(deviceId)
                                            putUserName(username)
                                            putUserIsLogin(true)
                                        }
                                        mLoginFlow.emit(Resource.Success(it))
                                    }
                                    3 -> mLoginFlow.emit(Resource.Success(it))
                                    else -> mLoginFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginRes>, t: Throwable) {
                        Method.logE(LoginRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mLoginFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiUserLogout(
    ) {
        Coroutines.io {
            LogoutReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                deviceId = dataStoreRepo.getDeviceId()
            ).apply {
                mLogoutFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiUserLogout(this).enqueue(object : Callback<LogoutRes> {
                    override fun onResponse(call: Call<LogoutRes>, response: Response<LogoutRes>) {
                        Method.logE(LogoutRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        dataStoreRepo.clearData()
                                        deleteDbData()
                                        mLogoutFlow.emit(Resource.Success(it))
                                    }
                                    else -> mLogoutFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<LogoutRes>, t: Throwable) {
                        Method.logE(LogoutRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mLogoutFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiUserRegister(
        username: String,
        password: String,
        deviceId: String
    ) {
        Coroutines.io {
            RegisterReq(
                username = username,
                password = AES.encrypt(Constants.MMSLAB, password),
                deviceId = deviceId
            ).apply {
                mRegisterFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiUserRegister(this).enqueue(object : Callback<RegisterRes> {
                    override fun onResponse(call: Call<RegisterRes>, response: Response<RegisterRes>) {
                        Method.logE(RegisterRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mRegisterFlow.emit(Resource.Success(it))
                                    else -> mRegisterFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<RegisterRes>, t: Throwable) {
                        Method.logE(RegisterRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mRegisterFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiDeleteAccount(
    ) {
        Coroutines.io {
            DeleteAccountReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID()
            ).apply {
                mDeleteAccountFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiDeleteAccount(this).enqueue(object : Callback<DeleteAccountRes> {
                    override fun onResponse(call: Call<DeleteAccountRes>, response: Response<DeleteAccountRes>) {
                        Method.logE(DeleteAccountRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        dataStoreRepo.run {
                                            clearData()
                                            clearPublicData()
                                            deleteDbData()
                                        }
                                        mDeleteAccountFlow.emit(Resource.Success(it))
                                    }
                                    else -> mDeleteAccountFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<DeleteAccountRes>, t: Throwable) {
                        Method.logE(DeleteAccountRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mDeleteAccountFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiAddFcmToken(
        fcmToken: String
    ) {
        Coroutines.io {
            AddFcmTokenReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                deviceId = dataStoreRepo.getDeviceId(),
                fcmToken = fcmToken
            ).apply {
                mPutFcmTokenFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiAddFcmToken(this).enqueue(object : Callback<AddFcmTokenRes> {
                    override fun onResponse(call: Call<AddFcmTokenRes>, response: Response<AddFcmTokenRes>) {
                        Method.logE(AddFcmTokenRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPutFcmTokenFlow.emit(Resource.Success(it))
                                    else -> mPutFcmTokenFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<AddFcmTokenRes>, t: Throwable) {
                        Method.logE(AddFcmTokenRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPutFcmTokenFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiSetUserImage(
        userImage: String
    ) {
        Coroutines.io {
            SetUserImageReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                userImage = userImage
            ).apply {
                mSetUserImageFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiSetUserImage(this).enqueue(object : Callback<SetUserImageRes> {
                    override fun onResponse(call: Call<SetUserImageRes>, response: Response<SetUserImageRes>) {
                        Method.logE(SetUserImageRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        dataStoreRepo.putUserPicture(userImage)
                                        mSetUserImageFlow.emit(Resource.Success(it))
                                    }
                                    else -> mSetUserImageFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<SetUserImageRes>, t: Throwable) {
                        Method.logE(SetUserImageRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mSetUserImageFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiGetUserImage(
    ) {
        Coroutines.io {
            delay(500)
            GetUserImageReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
            ).apply {
                mGetUserImageFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiGetUserImage(this).enqueue(object : Callback<GetUserImageRes> {
                    override fun onResponse(call: Call<GetUserImageRes>, response: Response<GetUserImageRes>) {
                        Method.logE(GetUserImageRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        dataStoreRepo.putUserPicture(it.result.userImage)
                                        mGetUserImageFlow.emit(Resource.Success(it))
                                    }
                                    else -> mGetUserImageFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<GetUserImageRes>, t: Throwable) {
                        Method.logE(GetUserImageRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mGetUserImageFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiSetUserPassword(
        password: String
    ) {
        Coroutines.io {
            SetPasswordReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                password = AES.encrypt(Constants.MMSLAB, password)
            ).apply {
                mSetUserPasswordFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiSetUserPassword(this).enqueue(object : Callback<SetPasswordRes> {
                    override fun onResponse(call: Call<SetPasswordRes>, response: Response<SetPasswordRes>) {
                        Method.logE(SetPasswordRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        dataStoreRepo.putPassword(password)
                                        mSetUserPasswordFlow.emit(Resource.Success(it))
                                    }
                                    else -> mSetUserPasswordFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<SetPasswordRes>, t: Throwable) {
                        Method.logE(SetPasswordRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mSetUserPasswordFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiPushFavorite(
        placeIdList: ArrayList<String>
    ) {
        Coroutines.io {
            PushFavoriteReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                favoriteList = placeIdList
            ).apply {
                mPushFavoriteListFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiPushFavorite(this).enqueue(object : Callback<PushFavoriteRes> {
                    override fun onResponse(call: Call<PushFavoriteRes>, response: Response<PushFavoriteRes>) {
                        Method.logE(PushFavoriteRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPushFavoriteListFlow.emit(Resource.Success(it))
                                    else -> mPushFavoriteListFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PushFavoriteRes>, t: Throwable) {
                        Method.logE(PushFavoriteRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPushFavoriteListFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiPullFavorite(
        placeIdList: ArrayList<String>
    ) {
        Coroutines.io {
            PullFavoriteReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                favoriteIdList = placeIdList
            ).apply {
                mPullFavoriteListFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiPullFavorite(this).enqueue(object : Callback<PullFavoriteRes> {
                    override fun onResponse(call: Call<PullFavoriteRes>, response: Response<PullFavoriteRes>) {
                        Method.logE(PullFavoriteRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPullFavoriteListFlow.emit(Resource.Success(it))
                                    else -> mPullFavoriteListFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PullFavoriteRes>, t: Throwable) {
                        Method.logE(PullFavoriteRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPullFavoriteListFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiSyncFavoriteList(
    ) {
        Coroutines.io {
            syncFavoriteList().distinctUntilChanged().collect { favoriteLists ->
                mGetSyncFavoriteListFlow.emit(favoriteLists)
            }
        }
    }

    fun apiPushBlackList(
        placeIdList: ArrayList<String>
    ) {
        Coroutines.io {
            PushBlackListReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                placeIdList = placeIdList
            ).apply {
                mPushBlackListFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiPushBlackList(this).enqueue(object : Callback<PushBlackListRes> {
                    override fun onResponse(call: Call<PushBlackListRes>, response: Response<PushBlackListRes>) {
                        Method.logE(PushBlackListRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPushBlackListFlow.emit(Resource.Success(it))
                                    else -> mPushBlackListFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PushBlackListRes>, t: Throwable) {
                        Method.logE(PushBlackListRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPushBlackListFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiPullBlackList(
        placeIdList: ArrayList<String>
    ) {
        Coroutines.io {
            PullBlackListReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                placeIdList = placeIdList
            ).apply {
                mPullBlackListFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiPullBlackList(this).enqueue(object : Callback<PullBlackListRes> {
                    override fun onResponse(call: Call<PullBlackListRes>, response: Response<PullBlackListRes>) {
                        Method.logE(PullBlackListRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPullBlackListFlow.emit(Resource.Success(it))
                                    else -> mPullBlackListFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PullBlackListRes>, t: Throwable) {
                        Method.logE(PullBlackListRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPullBlackListFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiSyncBlackList(
    ) {
        Coroutines.io {
            syncBlackList().distinctUntilChanged().collect { placeLists ->
                mGetSyncBlackListFlow.emit(placeLists)
            }
        }
    }

    fun apiPushPlaceList(
        place_id: String,
        name: String,
        address: String,
        location: Location
    ) {
        Coroutines.io {
            PushPlaceListReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                place_id = place_id,
                name = name,
                address = address,
                location = location
            ).apply {
                mPushPlaceListFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiPushPlaceList(this).enqueue(object : Callback<PushPlaceListRes> {
                    override fun onResponse(call: Call<PushPlaceListRes>, response: Response<PushPlaceListRes>) {
                        Method.logE(PushPlaceListRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPushPlaceListFlow.emit(Resource.Success(it))
                                    else -> mPushPlaceListFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PushPlaceListRes>, t: Throwable) {
                        Method.logE(PushPlaceListRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPushPlaceListFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiPullPlaceList(
        place_id: String
    ) {
        Coroutines.io {
            PullPlaceListReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                place_id = place_id
            ).apply {
                mPullPlaceListFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiPullPlaceList(this).enqueue(object : Callback<PullPlaceListRes> {
                    override fun onResponse(call: Call<PullPlaceListRes>, response: Response<PullPlaceListRes>) {
                        Method.logE(PullPlaceListRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPullPlaceListFlow.emit(Resource.Success(it))
                                    else -> mPullPlaceListFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PullPlaceListRes>, t: Throwable) {
                        Method.logE(PullPlaceListRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPullPlaceListFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiSyncPlaceList(
    ) {
        Coroutines.io {
            syncPlaceList().distinctUntilChanged().collect { placeLists ->
                mGetSyncPlaceListFlow.emit(placeLists)
            }
        }
    }

    /**
     * 同步機制(以遠端為準)
     */
    private fun apiGetFavorite() : Flow<List<FavoriteList>> = callbackFlow {
        GetFavoriteReq(
            accessKey = dataStoreRepo.getAccessKey(),
            userId = dataStoreRepo.getUserUID()
        ).apply {
            ApiClient.getAPI.apiGetFavorite(this).enqueue(object : Callback<GetFavoriteRes> {
                override fun onResponse(call: Call<GetFavoriteRes>, response: Response<GetFavoriteRes>) {
                    Method.logE(GetFavoriteRes::class.java.simpleName, "SUCCESS")
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                trySend(it.result.placeList)
                                mGetFavoriteListData.postValue(Resource.Success(it))
                            }
                            else -> mGetFavoriteListData.postValue(Resource.Error(it.errMsg.toString()))
                        }
                    }
                }

                override fun onFailure(call: Call<GetFavoriteRes>, t: Throwable) {
                    Method.logE(GetFavoriteRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                    mGetFavoriteListData.postValue(Resource.Error(t.message.toString()))
                }
            })
        }
        awaitClose { getFavoriteListData.value is Resource.Error || getFavoriteListData.value is Resource.Success }
    }

    private fun syncFavoriteList(): Flow<List<FavoriteList>> {
        val favoriteList = getFavoriteRepo.getData()
        return apiGetFavorite()
            .onStart { emit(favoriteList) }
            .onEach { favoriteLists ->
                if (favoriteLists != favoriteList)
                    getFavoriteRepo.insertAllData(favoriteLists)
            }
    }

    private fun apiGetBlackList() : Flow<List<PlaceList>> = callbackFlow {
        GetBlackListReq(
            accessKey = dataStoreRepo.getAccessKey(),
            userId = dataStoreRepo.getUserUID()
        ).apply {
            ApiClient.getAPI.apiGetBlackList(this).enqueue(object : Callback<GetBlackListRes> {
                override fun onResponse(call: Call<GetBlackListRes>, response: Response<GetBlackListRes>) {
                    Method.logE(GetBlackListRes::class.java.simpleName, "SUCCESS")
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                trySend(it.result.placeList)
                                mGetBlackListData.postValue(Resource.Success(it))
                            }
                            else -> mGetBlackListData.postValue(Resource.Error(it.errMsg.toString()))
                        }
                    }
                }

                override fun onFailure(call: Call<GetBlackListRes>, t: Throwable) {
                    Method.logE(GetBlackListRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                    mGetBlackListData.postValue(Resource.Error(t.message.toString()))
                }
            })
        }
        awaitClose { getBlackListData.value is Resource.Error || getBlackListData.value is Resource.Success }
    }

    private fun syncBlackList(): Flow<List<PlaceList>> {
        val placeList = getBlackListRepo.getData()
        return apiGetBlackList()
            .onStart { emit(placeList) }
            .onEach { placeLists ->
                if (placeLists != placeList)
                    getBlackListRepo.insertAllData(placeLists)
            }
    }

    private fun apiGetPlaceList() : Flow<List<MyPlaceList>> = callbackFlow {
        GetPlaceListReq(
            accessKey = dataStoreRepo.getAccessKey(),
            userId = dataStoreRepo.getUserUID()
        ).apply {
            ApiClient.getAPI.apiGetPlaceList(this).enqueue(object : Callback<GetPlaceListRes> {
                override fun onResponse(call: Call<GetPlaceListRes>, response: Response<GetPlaceListRes>) {
                    Method.logE(GetPlaceListRes::class.java.simpleName, "SUCCESS")
                    response.body()?.let {
                        when (it.status) {
                            0 -> {
                                trySend(it.result.placeList)
                                mGetPlaceListData.postValue(Resource.Success(it))
                            }
                            else -> mGetPlaceListData.postValue(Resource.Error(it.errMsg.toString()))
                        }
                    }
                }

                override fun onFailure(call: Call<GetPlaceListRes>, t: Throwable) {
                    Method.logE(GetPlaceListRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                    mGetPlaceListData.postValue(Resource.Error(t.message.toString()))
                }
            })
        }
        awaitClose { getPlaceListData.value is Resource.Error || getPlaceListData.value is Resource.Success }
    }

    private fun syncPlaceList(): Flow<List<MyPlaceList>> {
        val placeList = getPlaceListRepo.getData()
        return apiGetPlaceList()
            .onStart { emit(placeList) }
            .onEach { placeLists ->
                if (placeLists != placeList)
                    getPlaceListRepo.insertAllData(placeLists)
            }
    }

    /**
     * 其他
     */
    private fun deleteDbData() {
        Coroutines.io {
            distanceSearchRepo.deleteData()
            drawCardRepo.deleteData()
            getFavoriteRepo.deleteAllData()
            getBlackListRepo.deleteAllData()
            getPlaceListRepo.deleteAllData()
            historySearchRepo.deleteAllData()
        }
    }
}