package mai.project.foodmap.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import mai.project.core.extensions.displayToast
import mai.project.core.utils.Event
import mai.project.foodmap.R
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.utils.handleResult
import timber.log.Timber

/**
 * 基礎 BottomSheetDialog ，用於繼承
 */
abstract class BaseBottomSheetDialog<VB : ViewBinding, VM : BaseViewModel>(
    private val bindingInflater: (LayoutInflater) -> VB
) : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    protected open val viewModel: VM? = null

    /**
     * 取得 NavController
     */
    protected lateinit var navController: NavController
        private set

    // region public function
    /**
     * 處理 API 基礎回傳結果
     *
     * @param event API 回傳事件
     * @param workOnSuccess 成功後執行工作
     * @param workOnError 失敗後執行工作
     */
    protected fun <T> handleBasicResult(
        event: Event<NetworkResult<T>>,
        workOnSuccess: ((T?) -> Unit)? = null,
        workOnError: (() -> Unit)? = null
    ) {
        event.getContentIfNotHandled?.handleResult {
            onLoading = { viewModel?.setLoading(true) }
            onSuccess = {
                viewModel?.setLoading(false)
                workOnSuccess?.invoke(it)
            }
            onError = { _, msg ->
                viewModel?.setLoading(false)
                requireContext().displayToast(msg ?: "Unknown Error")
                workOnError?.invoke()
            }
        }
    }
    // endregion public function

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            // 完全展開 BottomSheet
            setOnShowListener { dialog ->
                val bottomSheetDialog = dialog as BottomSheetDialog
                bottomSheetDialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )?.also { fl ->
                    BottomSheetBehavior.from(fl).state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d(message = "${this::class.simpleName} onCreateView")
        _binding = bindingInflater.invoke(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d(message = "${this::class.simpleName} onViewCreated")
        navController = findNavController()

        // Setup Basic Function
        binding.initialize(savedInstanceState)
        binding.setObserver()
        binding.setListener()
    }

    override fun onStart() {
        super.onStart()
        Timber.d(message = "${this::class.simpleName} onStart")
    }

    override fun onResume() {
        super.onResume()
        Timber.d(message = "${this::class.simpleName} onResume")
    }

    override fun onPause() {
        super.onPause()
        Timber.d(message = "${this::class.simpleName} onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d(message = "${this::class.simpleName} onDestroyView")
        binding.destroy()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d(message = "${this::class.simpleName} onDestroy")
    }

    // region: open function
    /**
     * 初始化用的函式，可在此設定初始值。
     */
    open fun VB.initialize(savedInstanceState: Bundle?) {}

    /**
     * 建立觀察者用的函式，可在此設定觀察者。
     */
    open fun VB.setObserver() {}

    /**
     * 設定監聽器用的函式，可在此設定監聽器。
     */
    open fun VB.setListener() {}

    /**
     * 銷毀用的函式，可在Fragment銷毀時，同時移除不需要的事件。
     */
    open fun VB.destroy() {}
    // endregion
}