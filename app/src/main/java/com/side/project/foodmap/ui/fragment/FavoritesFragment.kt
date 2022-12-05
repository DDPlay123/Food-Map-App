package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.FragmentFavoritesBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.helper.show
import com.side.project.foodmap.ui.adapter.FavoriteListAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Method.logE
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>(R.layout.fragment_favorites) {
    private val viewModel: MainViewModel by activityViewModel()

    // Data
    private lateinit var remoteFavoriteList: List<FavoriteList>

    // Toole
    private lateinit var favoriteListAdapter: FavoriteListAdapter

    override fun FragmentFavoritesBinding.initialize() {
        initLocationService()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        initRvFavoriteList()
        setListener()
    }

    private fun doInitialize() {
        viewModel.getFavoriteList()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 取的最愛清單
                launch {
                    viewModel.getFavoriteListState.observe(viewLifecycleOwner) { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                logE("Get Favorite List", "Loading")
                                dialog.showLoadingDialog(false)
                                binding.rvFavorites.hidden()
                                binding.lottieNoData.show()
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Get Favorite List", "Success")
                                dialog.cancelLoadingDialog()
                                binding.rvFavorites.show()
                                binding.lottieNoData.hidden()
                                resource.data?.let { data ->
                                    if (data.result.isNotEmpty())
                                        remoteFavoriteList = data.result
                                    else {
                                        binding.rvFavorites.hidden()
                                        binding.lottieNoData.show()
                                    }
                                }
                                return@observe
                            }
                            is Resource.Error -> {
                                logE("Get Favorite List", "Error：${resource.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                                return@observe
                            }
                            else -> Unit
                        }
                    }
                }
                // 取的最愛清單 From Room
                launch {
                    viewModel.observeFavoriteListFromRoom().observe(viewLifecycleOwner) { favoriteList ->
                        favoriteList?.let { favoriteListAdapter.setData(it) }
                        if (::remoteFavoriteList.isInitialized && favoriteList.size != remoteFavoriteList.size) {
                            // TODO(同步兩邊)
                        }
                    }
                }
                // 刪除最愛
                launch {
                    viewModel.pullFavoriteState.collect {
                        when (it) {
                            is Resource.Success -> {
                                logE("Pull Favorite", "Success")
                            }
                            is Resource.Error -> {
                                logE("Pull Favorite", "Error:${it.message.toString()}")
                                requireActivity().displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun setListener() {

    }

    private fun initRvFavoriteList() {
        favoriteListAdapter = FavoriteListAdapter()
        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = favoriteListAdapter

            setItemListener()
        }
    }

    private fun setItemListener() {
        if (!::favoriteListAdapter.isInitialized) return

        favoriteListAdapter.onItemPullFavorite = { item, position ->
            viewModel.deleteFavoriteData(item)
            viewModel.pullFavorite(arrayListOf(item.placeId))
        }
    }
}