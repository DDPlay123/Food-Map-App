package mai.project.foodmap.features.restaurant_feature.searchDialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.foodmap.base.BaseBottomSheetDialog
import mai.project.foodmap.databinding.DialogBottomSheetSearchBinding

@AndroidEntryPoint
class SearchBottomSheetDialog : BaseBottomSheetDialog<DialogBottomSheetSearchBinding, SearchViewModel>(
    bindingInflater = DialogBottomSheetSearchBinding::inflate
) {
    override val viewModel by viewModels<SearchViewModel>()

    private val args by navArgs<SearchBottomSheetDialogArgs>()

    override fun DialogBottomSheetSearchBinding.initialize(savedInstanceState: Bundle?) {
        edSearch.setText(args.keyword)
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(it)
            behavior.skipCollapsed = true
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun DialogBottomSheetSearchBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(

        )
    }

    override fun DialogBottomSheetSearchBinding.setListener() {
        tvClear.onClick(anim = true) {

        }
    }
}