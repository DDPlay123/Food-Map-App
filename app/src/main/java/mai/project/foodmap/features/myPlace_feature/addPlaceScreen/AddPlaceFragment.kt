package mai.project.foodmap.features.myPlace_feature.addPlaceScreen

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentAddPlaceBinding

@AndroidEntryPoint
class AddPlaceFragment : BaseFragment<FragmentAddPlaceBinding, AddPlaceViewModel>(
    bindingInflater = FragmentAddPlaceBinding::inflate
) {
    override val viewModel by viewModels<AddPlaceViewModel>()
}