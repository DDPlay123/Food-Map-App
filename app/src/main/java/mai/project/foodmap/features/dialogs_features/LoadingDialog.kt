package mai.project.foodmap.features.dialogs_features

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseDialog
import mai.project.foodmap.databinding.DialogLoadingBinding

@AndroidEntryPoint
class LoadingDialog : BaseDialog<DialogLoadingBinding, Nothing>(
    bindingInflater = DialogLoadingBinding::inflate
) {
    private val args by navArgs<LoadingDialogArgs>()

    override fun DialogLoadingBinding.initialize(savedInstanceState: Bundle?) {
        isCancelable = args.cancelable
    }
}