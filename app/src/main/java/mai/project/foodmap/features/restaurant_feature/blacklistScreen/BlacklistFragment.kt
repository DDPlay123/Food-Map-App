package mai.project.foodmap.features.restaurant_feature.blacklistScreen

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.DP
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.widget.recyclerView_decorations.GridItemDecoration
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.base.handleBasicResult
import mai.project.foodmap.base.navigateLoadingDialog
import mai.project.foodmap.databinding.FragmentBlacklistBinding
import mai.project.foodmap.domain.models.MyBlacklistResult

@AndroidEntryPoint
class BlacklistFragment : BaseFragment<FragmentBlacklistBinding, BlacklistViewModel>(
    bindingInflater = FragmentBlacklistBinding::inflate
) {
    override val viewModel by viewModels<BlacklistViewModel>()

    private val blacklistAdapter by lazy { BlacklistAdapter() }

    override fun FragmentBlacklistBinding.initialize(savedInstanceState: Bundle?) {
        with(rvRestaurants) {
            addItemDecoration(
                GridItemDecoration(
                    spanCount = (rvRestaurants.layoutManager as GridLayoutManager).spanCount,
                    space = 10.DP,
                    sideSpace = 10.DP
                )
            )
            adapter = blacklistAdapter
        }

        viewModel.fetchMyBlacklist()
    }

    override fun FragmentBlacklistBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // Loading
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 抓取儲存的黑名單
            { myBlacklistResult.collect(::handleBasicResult) },
            // 黑名單列表
            { myBlacklist.collect(::handleBlacklist) }
        )
    }

    override fun FragmentBlacklistBinding.setListener() {
        imgBack.onClick { navigateUp() }

        swipeRefresh.setOnRefreshListener {
            viewModel.fetchMyBlacklist()
            swipeRefresh.isRefreshing = false
        }
        
        blacklistAdapter.onItemClick = {
            navigate(
                BlacklistFragmentDirections.actionBlacklistFragmentToRestaurantDetailFragment(
                    placeId = it.placeId,
                    name = it.name,
                    lat = it.lat.toFloat(),
                    lng = it.lng.toFloat()
                )
            )
        }
    }

    /**
     * 設定黑名單列表資料
     */
    private fun handleBlacklist(list: List<MyBlacklistResult>) = with(binding) {
        lottieNoData.isVisible = list.isEmpty()
        blacklistAdapter.submitList(list)
    }
}