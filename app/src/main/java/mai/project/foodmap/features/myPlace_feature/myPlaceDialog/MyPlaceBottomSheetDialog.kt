package mai.project.foodmap.features.myPlace_feature.myPlaceDialog

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.getColorCompat
import mai.project.core.extensions.getDrawableCompat
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.widget.recyclerView_decorations.DividerItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseBottomSheetDialog
import mai.project.foodmap.databinding.DialogBottomSheetMyPlaceBinding
import mai.project.foodmap.domain.models.MyPlaceResult

@AndroidEntryPoint
class MyPlaceBottomSheetDialog : BaseBottomSheetDialog<DialogBottomSheetMyPlaceBinding, MyPlaceViewModel>(
    bindingInflater = DialogBottomSheetMyPlaceBinding::inflate
) {
    override val viewModel by viewModels<MyPlaceViewModel>()

    private val args by navArgs<MyPlaceBottomSheetDialogArgs>()

    private val myPlaceAdapter by lazy { MyPlaceAdapter() }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            // 已選擇的項目無法滑動
            if (viewHolder.itemViewType == MyPlaceAdapter.SELECTED_ITEM) {
                return 0
            }
            return makeMovementFlags(0, ItemTouchHelper.LEFT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false // 不處理拖動

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = .7f

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val list = myPlaceAdapter.currentList.toMutableList()
            val placeId = list[position].placeId
            list.removeAt(position)
            myPlaceAdapter.submitList(list)
            viewModel.pullMyPlace(placeId)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            if (dX < 0) {
                // 畫紅色背景
                val p = Paint().apply { color = getColorCompat(R.color.error) }
                val background = RectF(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                c.drawRect(background, p)

                // 取得 icon
                val icon = getDrawableCompat(R.drawable.vector_delete) ?: return

                // 計算露出區域寬度（正值）
                val uncoveredWidth = -dX
                // 將 icon 置中於該區域
                val iconWidth = icon.intrinsicWidth
                val iconHeight = icon.intrinsicHeight
                val iconLeft = itemView.right + dX + (uncoveredWidth - iconWidth) / 2
                val iconTop = itemView.top + (itemHeight - iconHeight) / 2
                val iconRight = iconLeft + iconWidth
                val iconBottom = iconTop + iconHeight

                icon.setBounds(
                    iconLeft.toInt(),
                    iconTop,
                    iconRight.toInt(),
                    iconBottom
                )
                icon.draw(c)
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

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
            ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
            adapter = myPlaceAdapter
        }
    }

    override fun DialogBottomSheetMyPlaceBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 抓取儲存的定位點資訊
            { myPlaceListResult.collect(::handleBasicResult) },
            // 移除定位點資訊
            { pullMyPlaceResult.collect(::handleBasicResult) },
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