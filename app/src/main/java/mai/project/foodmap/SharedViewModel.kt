package mai.project.foodmap

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import mai.project.core.utils.CoroutineContextProvider
import mai.project.core.utils.WhileSubscribedOrRetained
import mai.project.foodmap.base.BaseViewModel
import mai.project.foodmap.domain.repository.PreferenceRepo
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
    private val preferenceRepo: PreferenceRepo
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
}