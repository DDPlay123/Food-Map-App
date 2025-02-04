package mai.project.foodmap.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import mai.project.core.utils.CoroutineContextProvider
import mai.project.foodmap.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider
) : BaseViewModel(contextProvider) {

    val isLogin = false
}