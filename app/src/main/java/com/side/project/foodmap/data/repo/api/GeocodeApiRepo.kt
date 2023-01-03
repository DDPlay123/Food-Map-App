package com.side.project.foodmap.data.repo.api

import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.data.remote.SetLocation
import com.side.project.foodmap.data.remote.geocode.*
import com.side.project.foodmap.data.repo.DataStoreRepo
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

class GeocodeApiRepo : KoinComponent {
    private val dataStoreRepo: DataStoreRepo by inject()

    private val mLocationAutoCompleteFlow = MutableSharedFlow<Resource<AutoCompleteByLocationRes>>()
    val locationAutoCompleteFlow
        get() = mLocationAutoCompleteFlow.asSharedFlow()

    private val mKeywordAutoCompleteFlow = MutableSharedFlow<Resource<AutoCompleteByKeywordRes>>()
    val keywordAutoCompleteFlow
        get() = mKeywordAutoCompleteFlow.asSharedFlow()

    private val mGetLocationFlow = MutableSharedFlow<Resource<GetLocationByAddressRes>>()
    val getLocationFlow
        get() = mGetLocationFlow.asSharedFlow()

    private val mGetRoutePolyline = MutableSharedFlow<Resource<GetRoutePolylineRes>>()
    val getRoutePolyline
        get() = mGetRoutePolyline.asSharedFlow()

    fun apiGeocodeAutocompleteByLocation(
        location: Location,
        input: String = ""
    ) {
        Coroutines.io {
            AutoCompleteByLocationReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                location = location,
                input = input
            ).apply {
                mLocationAutoCompleteFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiGeocodeAutocompleteByLocation(this).enqueue(object : Callback<AutoCompleteByLocationRes> {
                    override fun onResponse(call: Call<AutoCompleteByLocationRes>, response: Response<AutoCompleteByLocationRes>) {
                        Method.logE(AutoCompleteByLocationRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mLocationAutoCompleteFlow.emit(Resource.Success(it))
                                    else -> mLocationAutoCompleteFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<AutoCompleteByLocationRes>, t: Throwable) {
                        Method.logE(AutoCompleteByLocationRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mLocationAutoCompleteFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiGeocodeAutocompleteByKeyword(
        location: Location,
        input: String
    ) {
        Coroutines.io {
            AutoCompleteByKeywordReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                location = location,
                input = input
            ).apply {
                mKeywordAutoCompleteFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiGeocodeAutocompleteByKeyword(this).enqueue(object : Callback<AutoCompleteByKeywordRes> {
                    override fun onResponse(call: Call<AutoCompleteByKeywordRes>, response: Response<AutoCompleteByKeywordRes>) {
                        Method.logE(AutoCompleteByKeywordRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mKeywordAutoCompleteFlow.emit(Resource.Success(it))
                                    else -> mKeywordAutoCompleteFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<AutoCompleteByKeywordRes>, t: Throwable) {
                        Method.logE(AutoCompleteByKeywordRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mKeywordAutoCompleteFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiGeocodeGetLocationByAddress(
        address: String
    ) {
        Coroutines.io {
            GetLocationByAddressReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                address = address
            ).apply {
                mGetLocationFlow.emit(Resource.Loading())
                ApiClient.getAPI.apiGeocodeGetLocationByAddress(this).enqueue(object : Callback<GetLocationByAddressRes> {
                    override fun onResponse(call: Call<GetLocationByAddressRes>, response: Response<GetLocationByAddressRes>) {
                        Method.logE(GetLocationByAddressRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mGetLocationFlow.emit(Resource.Success(it))
                                    else -> mGetLocationFlow.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<GetLocationByAddressRes>, t: Throwable) {
                        Method.logE(GetLocationByAddressRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mGetLocationFlow.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }

    fun apiGeocodeGetRoutePolyline(
        origin: SetLocation,
        destination: SetLocation
    ) {
        Coroutines.io {
            GetRoutePolylineReq(
                accessKey = dataStoreRepo.getAccessKey(),
                userId = dataStoreRepo.getUserUID(),
                origin = origin,
                destination = destination
            ).apply {
                mGetRoutePolyline.emit(Resource.Loading())
                ApiClient.getAPI.apiGeocodeGetRoutePolyline(this).enqueue(object : Callback<GetRoutePolylineRes> {
                    override fun onResponse(call: Call<GetRoutePolylineRes>, response: Response<GetRoutePolylineRes>) {
                        Method.logE(GetRoutePolylineRes::class.java.simpleName, "SUCCESS")
                        Coroutines.io {
                            response.body()?.let {
                                when (it.status) {
                                    0 -> mGetRoutePolyline.emit(Resource.Success(it))
                                    else -> mGetRoutePolyline.emit(Resource.Error(it.errMsg.toString()))
                                }
                            }
                        }
                    }
                    override fun onFailure(call: Call<GetRoutePolylineRes>, t: Throwable) {
                        Method.logE(GetRoutePolylineRes::class.java.simpleName, "ERROR:${t.message.toString()}")
                        Coroutines.io { mGetRoutePolyline.emit(Resource.Error(t.message.toString())) }
                    }
                })
            }
        }
    }
}