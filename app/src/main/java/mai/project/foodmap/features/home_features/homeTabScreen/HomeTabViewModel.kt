package mai.project.foodmap.features.home_features.homeTabScreen

import dagger.hilt.android.lifecycle.HiltViewModel
import mai.project.core.utils.CoroutineContextProvider
import mai.project.foodmap.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider,
) : BaseViewModel(contextProvider) {
}