package com.side.project.foodmap.data.repo.api

import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.restaurant.*
import com.side.project.foodmap.data.repo.DataStoreRepo
import com.side.project.foodmap.data.repo.DistanceSearchRepo
import com.side.project.foodmap.data.repo.DrawCardRepo
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.Coroutines
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class RestaurantApiRepo : KoinComponent {
    private val dataStoreRepo: DataStoreRepo by inject()
    private val distanceSearchRepo: DistanceSearchRepo by inject()
    private val drawCardRepo: DrawCardRepo by inject()

    private val mDistanceSearchFlow = MutableSharedFlow<Resource<DistanceSearchRes>>()
    val distanceSearchFlow
        get() = mDistanceSearchFlow.asSharedFlow()

    private val mKeywordSearchFlow = MutableSharedFlow<Resource<KeywordSearchRes>>()
    val keywordSearchFlow
       get() = mKeywordSearchFlow.asSharedFlow()

    private val mDrawCardFlow = MutableSharedFlow<Resource<DrawCardRes>>()
    val drawCardFlow
        get() = mDrawCardFlow.asSharedFlow()

    private val mPlaceDetailFlow = MutableSharedFlow<Resource<DetailsByPlaceIdRes>>()
    val placeDetailFlow
        get() = mPlaceDetailFlow.asSharedFlow()

    private val mAutoCompleteFlow = MutableSharedFlow<Resource<AutoCompleteRes>>()
    val autoCompleteFlow
        get() = mAutoCompleteFlow.asSharedFlow()

    fun apiRestaurantDistanceSearch(
        location: Location,
        distance: Int,
        skip: Int,
        limit: Int
    ) {
        Coroutines.io {
            DistanceSearchReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                location = location,
                distance = distance,
                skip = skip,
                limit = limit
            ).apply {
                mDistanceSearchFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiRestaurantDistanceSearch(this).enqueue(object : Callback<DistanceSearchRes> {
                    override fun onResponse(call: Call<DistanceSearchRes>, response: Response<DistanceSearchRes>) {
                        Method.logE(DistanceSearchRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        mDistanceSearchFlow.emit(Resource.Success(it))
                                        distanceSearchRepo.run {
                                            deleteData()
                                            insertData(it)
                                        }
                                    }
                                    else -> {
                                        mDistanceSearchFlow.emit(Resource.Error(it.errMsg.toString()))
                                        getDistanceSearchDataFromRoom()
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<DistanceSearchRes>, t: Throwable) {
                        Method.logE(DistanceSearchRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io {
                            mDistanceSearchFlow.emit(Resource.Error(t.message.toString()))
                            getDistanceSearchDataFromRoom()
                        }
                    }
                })
            }
        }
    }

    fun apiRestaurantKeywordSearch(
        location: Location,
        distance: Int,
        keyword: String,
        skip: Int,
        limit: Int
    ) {
        Coroutines.io {
            KeywordSearchReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                location = location,
                distance = distance,
                keyword = keyword,
                skip = skip,
                limit = limit
            ).apply {
                mKeywordSearchFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiRestaurantKeywordSearch(this).enqueue(object : Callback<KeywordSearchRes> {
                    override fun onResponse(call: Call<KeywordSearchRes>, response: Response<KeywordSearchRes>) {
                        Method.logE(KeywordSearchRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mKeywordSearchFlow.emit(Resource.Success(it))
                                    else -> mKeywordSearchFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<KeywordSearchRes>, t: Throwable) {
                        Method.logE(KeywordSearchRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mKeywordSearchFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiDrawCard(
        location: Location,
        mode: Int,
        num: Int
    ) {
        Coroutines.io {
            DrawCardReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                location = location,
                mode = mode,
                num = num
            ).apply {
                mDrawCardFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiDrawCard(this).enqueue(object : Callback<DrawCardRes> {
                    override fun onResponse(call: Call<DrawCardRes>, response: Response<DrawCardRes>) {
                        Method.logE(DrawCardRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> {
                                        mDrawCardFlow.emit(Resource.Success(it))
                                        drawCardRepo.run {
                                            deleteData()
                                            insertData(it)
                                        }
                                    }
                                    else -> {
                                        mDrawCardFlow.emit(Resource.Error(it.errMsg.toString()))
                                        getDrawCardData()
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<DrawCardRes>, t: Throwable) {
                        Method.logE(DrawCardRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io {
                            mDrawCardFlow.emit(Resource.Error(t.message.toString()))
                            getDrawCardData()
                        }
                    }
                })
            }
        }
    }

    fun apiDetailByPlaceId(
        place_id: String
    ) {
        Coroutines.io {
            DetailsByPlaceIdReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                place_id = place_id
            ).apply {
                mPlaceDetailFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiDetailByPlaceId(this).enqueue(object : Callback<DetailsByPlaceIdRes> {
                    override fun onResponse(call: Call<DetailsByPlaceIdRes>, response: Response<DetailsByPlaceIdRes>) {
                        Method.logE(DetailsByPlaceIdRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mPlaceDetailFlow.emit(Resource.Success(it))
                                    else -> mPlaceDetailFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<DetailsByPlaceIdRes>, t: Throwable) {
                        Method.logE(DetailsByPlaceIdRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mPlaceDetailFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiAutoComplete(
        location: Location,
        distance: Long, // 公尺
        input: String
    ) {
        Coroutines.io {
            AutoCompleteReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                location = location,
                distance = distance, // 公尺
                input = input
            ).apply {
                mAutoCompleteFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiAutoComplete(this).enqueue(object : Callback<AutoCompleteRes> {
                    override fun onResponse(call: Call<AutoCompleteRes>, response: Response<AutoCompleteRes>) {
                        Method.logE(AutoCompleteRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io { response.body()?.let {
                            when (it.status) {
                                0 -> mAutoCompleteFlow.emit(Resource.Success(it))
                                else -> mAutoCompleteFlow.emit(Resource.Error(it.errMsg.toString()))
                            }
                        } }
                    }

                    override fun onFailure(call: Call<AutoCompleteRes>, t: Throwable) {
                        Method.logE(AutoCompleteRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mAutoCompleteFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    /**
     * Room Database
     */
    fun getDistanceSearchDataFromRoom() {
        Coroutines.io {
            mDistanceSearchFlow.emit(Resource.Loading())
            try {
                distanceSearchRepo.getData().let {
                    Method.logE("Distance From Room", "SUCCESS")
                    mDistanceSearchFlow.emit(Resource.Success(it))
                }
            } catch (e: IOException) {
                Method.logE("Distance From Room", "ERROR:${e.message.toString()}")
                mDistanceSearchFlow.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    fun getDrawCardData() {
        Coroutines.io {
            mDrawCardFlow.emit(Resource.Loading())
            try {
                drawCardRepo.getData().let {
                    Method.logE("DrawCard From Room", "SUCCESS")
                    mDrawCardFlow.emit(Resource.Success(it))
                }
            } catch (e: IOException) {
                Method.logE("DrawCard From Room", "ERROR:${e.message.toString()}")
                mDrawCardFlow.emit(Resource.Error(e.message.toString()))
            }
        }
    }
}