package mai.project.foodmap.features.home_features.favoriteTabScreen

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import mai.project.core.annotations.Direction
import mai.project.core.annotations.NavigationMode
import mai.project.core.extensions.DP
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.openGoogleNavigation
import mai.project.core.extensions.openPhoneCall
import mai.project.core.extensions.openUrlWithBrowser
import mai.project.core.extensions.parcelable
import mai.project.core.extensions.shareLink
import mai.project.core.widget.recyclerView_decorations.DividerItemDecoration
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateLoadingDialog
import mai.project.foodmap.base.navigateSelectorDialog
import mai.project.foodmap.databinding.FragmentFavoriteTabBinding
import mai.project.foodmap.domain.models.MyFavoriteResult
import mai.project.foodmap.features.dialogs_features.selector.SelectorCallback
import mai.project.foodmap.features.dialogs_features.selector.SelectorModel

@AndroidEntryPoint
class FavoriteTabFragment : BaseFragment<FragmentFavoriteTabBinding, FavoriteTabViewModel>(
    bindingInflater = FragmentFavoriteTabBinding::inflate
) {
    override val viewModel by viewModels<FavoriteTabViewModel>()

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
            // Loading
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 抓取儲存的收藏清單
            { myFavoritesResult.collect(::handleBasicResult) },
            // 新增/ 移除 收藏
            { pushOrPullMyFavoriteResult.collect { handleBasicResult(it, needLoading = false) } },
            // 收藏清單
            {
                combine(myFavoriteList, myFavoritePlaceIdList, myBlacklistPlaceIdList) { favorites, favoriteIds, blacklistIds ->
                    favorites.map { it.copy(isFavorite = it.placeId in favoriteIds) }
                        .filter { it.placeId !in blacklistIds }
                }.collect(::handleFavoriteList)
            }
        )
    }

    override fun FragmentFavoriteTabBinding.setListener() {
        swipeRefresh.setOnRefreshListener {
            viewModel.fetchMyFavorites()
            swipeRefresh.isRefreshing = false
        }

        favoriteAdapter.onItemClick = {
            navigate(
                FavoriteTabFragmentDirections.actionFavoriteTabFragmentToRestaurantDetailFragment(
                    placeId = it.placeId,
                    name = it.name
                )
            )
        }

        favoriteAdapter.onPhotoClick = { list, photo ->
            val position = list.indexOfFirst { it == photo }.takeIf { it >= 0 } ?: 0
            (activity as? MainActivity)?.openPhotoPreview(list, position)
        }

        favoriteAdapter.onFavoriteClick = { viewModel.setFavoriteForList(it.placeId, !it.isFavorite) }

        favoriteAdapter.onNavigationClick = {
            viewModel.selectedItemByAddress = it
            navigateSelectorDialog(
                requestCode = REQUEST_CODE_NAVIGATION_MODE,
                title = it.name,
                items = navigationModeItems
            )
        }

        favoriteAdapter.onWebsiteClick = { requireActivity().openUrlWithBrowser(it) }

        favoriteAdapter.onPhoneClick = { requireActivity().openPhoneCall(it) }

        favoriteAdapter.onShareClick = { requireActivity().shareLink(getString(R.string.sentence_share_restaurant), it) }
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

    /**
     * 處理收藏清單列表
     */
    private fun handleFavoriteList(list: List<MyFavoriteResult>) = with(binding) {
        lottieNoData.isVisible = list.isEmpty()
        favoriteAdapter.submitList(list)
    }

    companion object {
        /**
         * 點擊導航模式 Dialog
         */
        private const val REQUEST_CODE_NAVIGATION_MODE = "REQUEST_CODE_NAVIGATION_MODE"
    }
}