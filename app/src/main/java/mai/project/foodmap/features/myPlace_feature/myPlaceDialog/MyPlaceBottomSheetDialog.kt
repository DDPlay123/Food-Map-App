package mai.project.foodmap.features.myPlace_feature.myPlaceDialog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.utils.ImageLoaderUtil
import mai.project.core.widget.recyclerView_decorations.DividerItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseBottomSheetDialog
import mai.project.foodmap.databinding.DialogBottomSheetMyPlaceBinding
import mai.project.foodmap.domain.models.MyPlaceResult
import javax.inject.Inject

@AndroidEntryPoint
class MyPlaceBottomSheetDialog : BaseBottomSheetDialog<DialogBottomSheetMyPlaceBinding, MyPlaceViewModel>(
    bindingInflater = DialogBottomSheetMyPlaceBinding::inflate
) {
    override val viewModel by viewModels<MyPlaceViewModel>()

    private val args by navArgs<MyPlaceBottomSheetDialogArgs>()

    @Inject
    lateinit var imageLoaderUtil: ImageLoaderUtil

    private val myPlaceAdapter by lazy { MyPlaceAdapter(imageLoaderUtil) }

    override fun DialogBottomSheetMyPlaceBinding.initialize(savedInstanceState: Bundle?) {
        with(rvMyPlace) {
            addItemDecoration(
                DividerItemDecoration(
                    context = requireContext(),
                    direction = Direction.VERTICAL,
                    dividerHeight = 1.DP,
                    marginLeft = 16.DP,
                    marginRight = 16.DP,
                    dividerDrawableRes = R.drawable.bg_divider
                )
            )
            adapter = myPlaceAdapter
        }
    }

    override fun DialogBottomSheetMyPlaceBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 抓取儲存的定位點資訊
            { myPlaceListResult.collect(::handleBasicResult) },
            // 當前定位點資訊
            { myPlaceList.combine(myPlaceId) { p0, p1 -> p0 to p1 }.collect { handleMyPlaceList(it.first, it.second) } }
        )
    }

    override fun DialogBottomSheetMyPlaceBinding.setListener() {
        tvCurrentLocation.onClick {
            setFragmentResult(
                args.requestCode,
                bundleOf(MyPlaceCallback.ARG_ITEM_CLICK to MyPlaceCallback.OnItemClick(""))
            )
            dismiss()
        }

        tvAdd.onClick {
            setFragmentResult(
                args.requestCode,
                bundleOf(MyPlaceCallback.ARG_ADD_ADDRESS to MyPlaceCallback.OnAddAddress)
            )
            dismiss()
        }

        myPlaceAdapter.onClickedPlace = {
            setFragmentResult(
                args.requestCode,
                bundleOf(MyPlaceCallback.ARG_ITEM_CLICK to MyPlaceCallback.OnItemClick(it.placeId))
            )
            dismiss()
        }
    }

    /**
     * 處理定位點列表資訊
     */
    private fun handleMyPlaceList(
        list: List<MyPlaceResult>,
        myPlaceId: String
    ) = with(binding) {
        val target = list.indexOfFirst { it.placeId == myPlaceId }
        myPlaceAdapter.submitList(list, myPlaceId) {
            if (target != -1) {
                rvMyPlace.post { rvMyPlace.smoothScrollToPosition(target) }
            }
        }
    }
}