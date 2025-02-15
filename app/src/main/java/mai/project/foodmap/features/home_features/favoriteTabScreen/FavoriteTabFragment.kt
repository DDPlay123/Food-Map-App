package mai.project.foodmap.features.home_features.favoriteTabScreen

import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.annotations.Direction
import mai.project.core.annotations.NavigationMode
import mai.project.core.extensions.DP
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.openGoogleNavigation
import mai.project.core.extensions.parcelable
import mai.project.core.widget.recyclerView_decorations.DividerItemDecoration
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateSelectorDialog
import mai.project.foodmap.databinding.FragmentFavoriteTabBinding
import mai.project.foodmap.features.dialogs_features.selector.SelectorCallback
import mai.project.foodmap.features.dialogs_features.selector.SelectorModel
import timber.log.Timber

@AndroidEntryPoint
class FavoriteTabFragment : BaseFragment<FragmentFavoriteTabBinding, FavoriteTabViewModel>(
    bindingInflater = FragmentFavoriteTabBinding::inflate
) {
    override val viewModel by hiltNavGraphViewModels<FavoriteTabViewModel>(R.id.nav_main)

    override val isNavigationVisible: Boolean = true

    override val useActivityOnBackPressed: Boolean = true

    private val favoriteAdapter by lazy { FavoriteAdapter() }

    private val navigationModeItems: List<SelectorModel> by lazy {
        val typedArray = resources.obtainTypedArray(R.array.navigation_mode_icon)
        val icons = (0 until typedArray.length()).map { typedArray.getResourceId(it, 0) }
        typedArray.recycle()
        resources.getStringArray(R.array.navigation_mode).mapIndexed { index, s ->
            SelectorModel(id = index, content = s, iconResId = icons[index])
        }
    }

    override fun FragmentFavoriteTabBinding.initialize(savedInstanceState: Bundle?) {
        with(rvFavorites) {
            addItemDecoration(
                SpacesItemDecoration(
                    direction = Direction.VERTICAL,
                    space = 20.DP,
                    startSpace = 40.DP,
                    endSpace = 20.DP
                )
            )
            addItemDecoration(
                DividerItemDecoration(
                    context = requireContext(),
                    direction = Direction.VERTICAL,
                    dividerHeight = 1.DP,
                    marginTop = 10.DP,
                    dividerDrawableRes = R.drawable.bg_divider
                )
            )
            adapter = favoriteAdapter
        }

        viewModel.fetchMyFavorites()
    }

    override fun FragmentFavoriteTabBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 抓取儲存的收藏清單
            { myFavoritesResult.collect(::handleBasicResult) },
            // 新增/ 移除 收藏
            { pushOrPullMyFavoriteResult.collect(::handleBasicResult) },
            // 收藏清單
            { myFavorites.collect { favoriteAdapter.submitList(it) } }
        )
    }

    override fun FragmentFavoriteTabBinding.setListener() {
        favoriteAdapter.onItemClick = {
            navigate(
                FavoriteTabFragmentDirections.actionFavoriteTabFragmentToRestaurantDetailFragment(
                    placeId = it.placeId,
                    name = it.name,
                    lat = it.lat.toFloat(),
                    lng = it.lng.toFloat()
                )
            )
        }
        
        favoriteAdapter.onPhotoClick = { list, photo ->
            val position = list.indexOfFirst { it == photo }.takeIf { it >= 0 } ?: 0
            (activity as? MainActivity)?.openPhotoPreview(list, position)
        }

        favoriteAdapter.onAddressClick = {
            viewModel.selectedItemByAddress = it
            Timber.d(message = navigationModeItems.toString())
            navigateSelectorDialog(
                requestCode = REQUEST_CODE_NAVIGATION_MODE,
                title = it.name,
                items = navigationModeItems
            )
        }
    }

    override fun FragmentFavoriteTabBinding.setCallback() {
        setFragmentResultListener(REQUEST_CODE_NAVIGATION_MODE) { _, bundle ->
            bundle.parcelable<SelectorCallback>(SelectorCallback.ARG_ITEM_CLICK)?.let { callback ->
                callback as SelectorCallback.OnItemClick
                viewModel.selectedItemByAddress?.let {
                    val latLng = LatLng(it.lat, it.lng)
                    when (callback.item) {
                        navigationModeItems[0] -> requireActivity().openGoogleNavigation(NavigationMode.CAR, latLng)
                        navigationModeItems[1] -> requireActivity().openGoogleNavigation(NavigationMode.BICYCLE, latLng)
                        navigationModeItems[2] -> requireActivity().openGoogleNavigation(NavigationMode.MOTORCYCLE, latLng)
                        navigationModeItems[3] -> requireActivity().openGoogleNavigation(NavigationMode.WALKING, latLng)
                    }
                }
            }
        }
    }

    companion object {
        /**
         * 點擊導航模式 Dialog
         */
        private const val REQUEST_CODE_NAVIGATION_MODE = "REQUEST_CODE_NAVIGATION_MODE"
    }
}