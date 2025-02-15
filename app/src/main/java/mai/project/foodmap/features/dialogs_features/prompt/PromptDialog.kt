package mai.project.foodmap.features.dialogs_features.prompt

import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.onClick
import mai.project.foodmap.base.BaseDialog
import mai.project.foodmap.databinding.DialogPromptBinding

@AndroidEntryPoint
class PromptDialog : BaseDialog<DialogPromptBinding, Nothing>(
    bindingInflater = DialogPromptBinding::inflate
) {
    private val args by navArgs<PromptDialogArgs>()

    override fun DialogPromptBinding.initialize(savedInstanceState: Bundle?) {
        tvTitle.text = args.title
        tvMessage.text = args.message
        tvConfirm.text = args.confirmText
        tvConfirm.isVisible = args.confirmText != null
        tvCancel.text = args.cancelText
        tvCancel.isVisible = args.cancelText != null
        edInput.isVisible = args.enableInput
        edInput.hint = args.inputHint
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(
            args.requestCode,
            bundleOf(PromptCallback.ARG_DISMISS to PromptCallback.OnDismiss)
        )
    }

    override fun DialogPromptBinding.setListener() {
        tvConfirm.onClick {
            setFragmentResult(
                args.requestCode,
                bundleOf(
                    PromptCallback.ARG_CONFIRM to PromptCallback.OnConfirm(
                        edInput.text?.trim().toString()
                    )
                )
            )
            dismiss()
        }

        tvCancel.onClick {
            setFragmentResult(
                args.requestCode,
                bundleOf(PromptCallback.ARG_CANCEL to PromptCallback.OnCancel)
            )
            dismiss()
        }
    }
}