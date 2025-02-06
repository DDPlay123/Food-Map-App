package mai.project.foodmap

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.Event
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.repository.PreferenceRepo
import mai.project.foodmap.domain.repository.UserRepo
import mai.project.foodmap.domain.state.NetworkResult
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val preferenceRepo: PreferenceRepo,
    private val userRepo: UserRepo,
) : BaseViewModel(contextProvider) {

    // region Preference State
    /**
     * 是否登入
     */
    val isLogin: SharedFlow<Boolean> = preferenceRepo.readUserId
        .map { userId -> userId.isNotEmpty() }
        .distinctUntilChanged()
        .catch { emit(false) }
        .flowOn(contextProvider.io)
        .shareIn(viewModelScope, WhileSubscribedOrRetained, 0)
    // endregion Preference State

    // region Public function
    /**
     * 清空系統所有資料
     */
    suspend fun clearAllData() {
        preferenceRepo.clearAll()
    }
    // endregion Public function

    // region Network State
    /**
     * 新增 FCM Token
     */
    private val _addFcmTokenResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val addFcmTokenResult = _addFcmTokenResult.asStateFlow()

    fun addFcmToken() = launchCoroutineIO {
        safeApiCallFlow { userRepo.addFcmToken() }
            .collect { result -> _addFcmTokenResult.update { Event(result) } }
    }

    /**
     * 取得使用者大頭貼
     */
    private val _getUserImageResult = MutableStateFlow<Event<NetworkResult<EmptyNetworkResult>>>(Event(NetworkResult.Idle()))
    val getUserImageResult = _getUserImageResult.asStateFlow()

    fun getUserImage() = launchCoroutineIO {
        safeApiCallFlow { userRepo.getUserImage() }
            .collect { result -> _getUserImageResult.update { Event(result) } }
    }
    // endregion Network State
}