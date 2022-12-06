package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.FavoriteList
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.FragmentFavoritesBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.hidden
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.ui.adapter.FavoriteListAdapter
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.tools.Method.logE
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>(R.layout.fragment_favorites) {
    private val viewModel: MainViewModel by activityViewModel()

    // Data
    private lateinit var remoteFavoriteList: List<FavoriteList>

    // Toole
    private lateinit var favoriteListAdapter: FavoriteListAdapter

    // Wait pull favorite list
    private lateinit var favoriteList: FavoriteList

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
                                dialog.showLoadingDialog(mActivity, false)
                                return@observe
                            }
                            is Resource.Success -> {
                                logE("Get Favorite List", "Success")
                                dialog.cancelLoadingDialog()
                                binding.rvFavorites.display()
                                binding.lottieNoData.hidden()
                                resource.data?.let { data ->
                                    if (data.result.isNotEmpty())
                                        remoteFavoriteList = data.result
                                    else {
                                        binding.rvFavorites.hidden()
                                        binding.lottieNoData.display()
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
                    viewModel.observeFavoriteListFromRoom.observe(viewLifecycleOwner) { favoriteList ->
                        favoriteList?.let { favoriteListAdapter.setData(it) }
                        if (::remoteFavoriteList.isInitialized && !favoriteList.containsAll(remoteFavoriteList)) {
//                            // TODO(同步兩邊，目前需優化)
//                            viewModel.deleteAllFavoriteData()
//                            remoteFavoriteList.forEach {
//                                viewModel.insertFavoriteData(it)
//                            }
                        }

                        if (favoriteList.isNotEmpty()) {
                            binding.rvFavorites.display()
                            binding.lottieNoData.hidden()
                        } else {
                            binding.rvFavorites.hidden()
                            binding.lottieNoData.display()
                        }
                    }
                }
                // 刪除最愛
                launch {
                    viewModel.pullFavoriteState.collect {
                        when (it) {
                            is Resource.Success -> {
                                logE("Pull Favorite", "Success")
                                if (::favoriteList.isInitialized)
                                    viewModel.deleteFavoriteData(favoriteList)
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
        binding.run {
            fabUpTool.setOnClickListener {
                val smoothScroller: RecyclerView.SmoothScroller =
                    object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }
                smoothScroller.targetPosition = 0
                rvFavorites.layoutManager?.startSmoothScroll(smoothScroller)
            }
        }
    }

    private fun initRvFavoriteList() {
        favoriteListAdapter = FavoriteListAdapter()
        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = favoriteListAdapter
            setRvItemListener()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstItemPosition: Int = (binding.rvFavorites.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (firstItemPosition >= 1)
                        binding.fabUpTool.display()
                    else
                        binding.fabUpTool.gone()
                }
            })
        }
    }

    private fun setRvItemListener() {
        if (!::favoriteListAdapter.isInitialized) return

        favoriteListAdapter.onItemPullFavorite = { item ->
            favoriteList = item
            displayRemoveFavoriteDialog()
        }
    }

    private fun displayRemoveFavoriteDialog() {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    imgPromptIcon.setImageResource(R.drawable.ic_favorite)
                    titleText = getString(R.string.hint_prompt_remove_favorite_title)
                    tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                    tvConfirm.setOnClickListener {
                        viewModel.pullFavorite(arrayListOf(favoriteList.placeId))
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }
}