package mai.project.foodmap.features.restaurant_feature.restaurantListScreen

import android.os.Bundle
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import mai.project.core.Configs
import mai.project.core.extensions.DP
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.widget.recyclerView_decorations.GridItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateLoadingDialog
import mai.project.foodmap.databinding.FragmentRestaurantListBinding
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult

@AndroidEntryPoint
class RestaurantListFragment : BaseFragment<FragmentRestaurantListBinding, RestaurantListViewModel>(
    bindingInflater = FragmentRestaurantListBinding::inflate
) {
    override val viewModel by viewModels<RestaurantListViewModel>()

    private val args by navArgs<RestaurantListFragmentArgs>()

    private val restaurantAdapter by lazy { RestaurantAdapter() }

    override fun FragmentRestaurantListBinding.initialize(savedInstanceState: Bundle?) {
        sbDistance.max = Configs.MAX_SEARCH_DISTANCE - Configs.MIN_SEARCH_DISTANCE

        with(rvRestaurants) {
            addItemDecoration(
                GridItemDecoration(
                    spanCount = (rvRestaurants.layoutManager as GridLayoutManager).spanCount,
                    space = 10.DP,
                    sideSpace = 10.DP
                )
            )
            adapter = restaurantAdapter
        }

        viewModel.refreshRestaurants(
            keyword = args.keyword,
            lat = args.lat.toDouble(),
            lng = args.lng.toDouble()
        )
    }

    override fun FragmentRestaurantListBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // Loading
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 是否顯示距離控制器
            { showDistanceController.collect { clController.isVisible = it } },
            // 搜尋的距離
            { searchDistance.collect(::handleSearchDistanceState) },
            // 搜尋餐廳資料
            { searchRestaurantsResult.collect(::handleBasicResult) },
            // 餐廳列表資料
            {
                combine(restaurantList, myFavoritePlaceIdList, myBlacklistPlaceIdList) { list, favoriteIds, blacklistIds ->
                    list.map { it.copy(isFavorite = it.placeId in favoriteIds) }
                        .filter { it.placeId !in blacklistIds }
                }.collect(::handleRestaurantList)
            }
        )
    }

    override fun FragmentRestaurantListBinding.setListener() {
        imgBack.onClick { navigateUp() }

        imgDistance.onClick(safe = false) { viewModel.toggleShowDistanceController() }

        swipeRefresh.setOnRefreshListener {
            viewModel.refreshRestaurants(
                keyword = args.keyword,
                lat = args.lat.toDouble(),
                lng = args.lng.toDouble()
            )
            swipeRefresh.isRefreshing = false
        }

        fabTop.onClick { rvRestaurants.smoothScrollToPosition(0) }

        sbDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = Configs.MIN_SEARCH_DISTANCE + progress
                viewModel.setSearchDistance(value.takeIf { it >= Configs.MIN_SEARCH_DISTANCE } ?: Configs.MIN_SEARCH_DISTANCE)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val newDistance = seekBar?.progress?.plus(Configs.MIN_SEARCH_DISTANCE) ?: Configs.MIN_SEARCH_DISTANCE
                if (newDistance > viewModel.mDistance / 1000) {
                    viewModel.increaseDistanceAndLoadRestaurants(
                        keyword = args.keyword,
                        lat = args.lat.toDouble(),
                        lng = args.lng.toDouble()
                    )
                } else {
                    viewModel.refreshRestaurants(
                        keyword = args.keyword,
                        lat = args.lat.toDouble(),
                        lng = args.lng.toDouble()
                    )
                }
            }
        })

        rvRestaurants.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstItemPosition: Int = (rvRestaurants.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                fabTop.isVisible = firstItemPosition >= 1

                // 檢查是否無法再向下滾動 (1 表示向下方向)
                if (!recyclerView.canScrollVertically(1) &&
                    viewModel.searchRestaurantsResult.value.getPeekContent !is NetworkResult.Loading) {
                    val currentCount = viewModel.restaurantList.value.size
                    if (currentCount < viewModel.totalRestaurantCount) {
                        viewModel.loadNextRestaurants(
                            keyword = args.keyword,
                            lat = args.lat.toDouble(),
                            lng = args.lng.toDouble(),
                            skip = currentCount
                        )
                    }
                }
            }
        })
    }

    /**
     * 處理搜尋的距離狀態
     */
    private fun handleSearchDistanceState(distance: Int) = with(binding) {
        sbDistance.progress = distance - Configs.MIN_SEARCH_DISTANCE
        tvKilometer.text = if (distance == Configs.MIN_SEARCH_DISTANCE)
            getString(R.string.word_nearby)
        else
            getString(R.string.format_kilometer, distance)
    }

    /**
     * 處理餐廳列表資料
     */
    private fun handleRestaurantList(list: List<RestaurantResult>) = with(binding) {
        tvTitle.text = getString(
            R.string.format_title_count,
            args.keyword.ifEmpty { getString(R.string.sentence_near_restaurant) },
            list.size, viewModel.totalRestaurantCount
        )
        lottieNoData.isVisible = list.isEmpty()
        restaurantAdapter.submitList(list)
    }
}