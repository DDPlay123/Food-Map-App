package com.side.project.foodmap.ui.viewModel

import androidx.lifecycle.viewModelScope
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListViewModel : BaseViewModel() {

    /**
     * 資料流
     */
    val _watchDetailState = MutableStateFlow<Resource<String>>(Resource.Unspecified())
    val watchDetailState
        get() = _watchDetailState.asStateFlow()

    /**
     * 可呼叫方法
     */
    fun watchDetail(placeId: String) {
        if (placeId.isNotEmpty())
            viewModelScope.launch { _watchDetailState.emit(Resource.Success(placeId)) }
        else
            viewModelScope.launch { _watchDetailState.emit(Resource.Error("")) }
    }

    fun keywordSearch() {

    }
}