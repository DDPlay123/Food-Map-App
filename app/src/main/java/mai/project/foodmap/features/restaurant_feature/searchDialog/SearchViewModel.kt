package mai.project.foodmap.features.restaurant_feature.searchDialog

import dagger.hilt.android.lifecycle.HiltViewModel
import mai.project.core.utils.CoroutineContextProvider
import mai.project.foodmap.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    override val contextProvider: CoroutineContextProvider
) : BaseViewModel(contextProvider) {
}