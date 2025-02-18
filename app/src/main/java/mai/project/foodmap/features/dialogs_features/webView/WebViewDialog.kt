package mai.project.foodmap.features.dialogs_features.webView

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseDialog
import mai.project.foodmap.databinding.DialogWebViewBinding

@AndroidEntryPoint
class WebViewDialog : BaseDialog<DialogWebViewBinding, Nothing>(
    bindingInflater = DialogWebViewBinding::inflate
) {
    private val args by navArgs<WebViewDialogArgs>()

    override val useFullScreen: Boolean = true

    override fun DialogWebViewBinding.initialize(savedInstanceState: Bundle?) {
        with(webView) {
            loadDataWithBaseURL(null, args.path, "text/html", "UTF-8", null)
        }
    }

    override fun onStart() {
        super.onStart()
        // 完全展開
        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(it)
            behavior.skipCollapsed = true
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }
}