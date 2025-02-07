package mai.project.foodmap.features.dialogs_features.selector

import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseBottomSheetDialog
import mai.project.foodmap.databinding.DialogBottomSheetSelectorBinding

@AndroidEntryPoint
class SelectorBottomSheetDialog : BaseBottomSheetDialog<DialogBottomSheetSelectorBinding, Nothing>(
    bindingInflater = DialogBottomSheetSelectorBinding::inflate
) {
    private val args by navArgs<SelectorBottomSheetDialogArgs>()

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(
            args.requestCode,
            bundleOf(SelectorCallback.ARG_DISMISS to SelectorCallback.OnDismiss)
        )
    }

    override fun DialogBottomSheetSelectorBinding.initialize(savedInstanceState: Bundle?) {
        args.items.forEachIndexed { index, item ->
            // Menu群組ID, 項目ID, 項目順序, 項目名稱
            navigationView.menu.add(0, item.id, index, item.content).apply {
                setIcon(item.iconResId)
            }
        }
    }

    override fun DialogBottomSheetSelectorBinding.setListener() {
        navigationView.setNavigationItemSelectedListener {
            args.items.find { item -> item.id == it.itemId }?.let { item ->
                setFragmentResult(
                    args.requestCode,
                    bundleOf(SelectorCallback.ARG_ITEM_CLICK to SelectorCallback.OnItemClick(item))
                )
            }
            dismiss()
            true
        }
    }
}