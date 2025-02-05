package mai.project.foodmap.features.auth_features

import dagger.hilt.android.lifecycle.HiltViewModel
import mai.project.core.utils.CoroutineContextProvider
import mai.project.foodmap.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
) : BaseViewModel(contextProvider) {
}