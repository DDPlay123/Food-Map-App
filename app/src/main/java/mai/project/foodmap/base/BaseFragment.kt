package mai.project.foodmap.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import mai.project.core.extensions.displayToast
import mai.project.core.utils.Event
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.utils.handleResult
import mai.project.foodmap.features.dialogs_features.loading.LoadingDialogDirections
import mai.project.foodmap.features.dialogs_features.prompt.PromptDialogDirections
import mai.project.foodmap.features.dialogs_features.selector.SelectorBottomSheetDialogDirections
import mai.project.foodmap.features.dialogs_features.selector.SelectorModel
import timber.log.Timber

/**
 * 基礎 Fragment ，用於繼承
 *
 * example：
 * ```
 * // 無 ViewModel
 * class SampleFragment : BaseFragment<FragmentSampleBinding, Nothing>(
 *    bindingInflater = FragmentSampleBinding::inflate
 * {
 *    ... // 不用覆寫 viewModel
 * }
 *
 * // 有 ViewModel
 * class SampleFragment : BaseFragment<FragmentSampleBinding, SharedViewModel>(
 *     bindingInflater = FragmentSampleBinding::inflate
 * ) {
 *     override val viewModel by activityViewModels<SharedViewModel>()
 * }
 * ```
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel>(
    private val bindingInflater: (LayoutInflater) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    protected open val viewModel: VM? = null

    /**
     * 取得 NavController
     */
    protected lateinit var navController: NavController
        private set

    /**
     * 是否顯示 NavigationView
     */
    protected open val isNavigationVisible: Boolean = false

    /**
     * 是否使用 Activity 的 onBackPressed
     */
    protected open val useActivityOnBackPressed: Boolean = false

    /**
     * 是否監聽鍵盤開關以自動調整 Layout
     */
    protected open val useKeyboardListener: Boolean = false

    /**
     * 是否鎖定螢幕方向
     */
    protected open val lockedDeviceOrientation: Boolean = false

    /**
     * 返回鍵監聽器
     */
    private var onBackPressedCallback: OnBackPressedCallback? = null

    // region public function
    /**
     * 判斷當前頁面是否為指定頁面，如果不是則返回上一頁。並執行指定的工作。
     *
     * @param fragmentId Fragment ID
     * @param work 要執行的工作
     */
    protected fun doSomethingOnCurrentPage(
        fragmentId: Int,
        work: () -> Unit
    ) {
        if (navController.currentBackStackEntry?.destination?.id != fragmentId) {
            popBackStack()
        }
        work.invoke()
    }

    /**
     * 處理 API 基礎回傳結果
     *
     * @param event API 回傳事件
     * @param needLoading 是否需要 Loading
     * @param workOnSuccess 成功後執行工作
     * @param workOnError 失敗後執行工作
     */
    protected fun <T> handleBasicResult(
        event: Event<NetworkResult<T>>,
        needLoading: Boolean = true,
        workOnSuccess: ((T?) -> Unit)? = null,
        workOnError: (() -> Unit)? = null
    ) {
        event.getContentIfNotHandled?.handleResult {
            onLoading = { if (needLoading) viewModel?.setLoading(true) }
            onSuccess = {
                if (needLoading) viewModel?.setLoading(false)
                workOnSuccess?.invoke(it)
            }
            onError = { _, msg ->
                if (needLoading) viewModel?.setLoading(false)
                displayToast(msg ?: "Unknown Error")
                workOnError?.invoke()
            }
        }
    }
    // endregion public function

    // region dialog
    /**
     * 開啟/關閉 Loading Dialog
     *
     * @param cancelable 是否可以點擊關閉 Dialog
     */
    fun navigateLoadingDialog(
        isOpen: Boolean,
        cancelable: Boolean = true
    ) {
        val currentDestId = navController.currentBackStackEntry?.destination?.id
        when {
            // 顯示 LoadingDialog (不重複導頁)
            isOpen && currentDestId != R.id.loadingDialog -> {
                navController.navigate(LoadingDialogDirections.actionGlobalToLoadingDialog(cancelable))
            }

            // 關閉 LoadingDialog
            !isOpen && currentDestId == R.id.loadingDialog -> {
                // 退回上一層
                navController.navigateUp()
            }

            else -> Unit
        }
    }

    /**
     * 開啟 Prompt Dialog
     *
     * @param requestCode 請求碼
     * @param title 標題
     * @param message 內文
     * @param confirmText 確認按鈕 (不顯示則為 Null)
     * @param cancelText 取消按鈕 (不顯示則為 Null)
     * @param enableInput 是否啟用輸入框
     * @param inputHint 輸入框提示文本 (不顯示則為 Null)
     */
    fun navigatePromptDialog(
        requestCode: String,
        title: String,
        message: String,
        confirmText: String? = getString(R.string.word_confirm),
        cancelText: String? = getString(R.string.word_cancel),
        enableInput: Boolean = false,
        inputHint: String? = null
    ) {
        navigate(
            PromptDialogDirections.actionGlobalToPromptDialog(
                requestCode = requestCode,
                title = title,
                message = message,
                confirmText = confirmText,
                cancelText = cancelText,
                enableInput = enableInput,
                inputHint = inputHint
            )
        )
    }

    /**
     * 開啟 Selector Dialog
     *
     * @param requestCode 請求碼
     * @param items 選項
     */
    fun navigateSelectorDialog(
        requestCode: String,
        items: List<SelectorModel>
    ) {
        navigate(
            SelectorBottomSheetDialogDirections.actionGlobalToSelectorBottomSheetDialog(
                requestCode = requestCode,
                items = items.toTypedArray()
            )
        )
    }
    // endregion

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
        binding.setCallback()
    }

    override fun onStart() {
        super.onStart()
        Timber.d(message = "${this::class.simpleName} onStart")
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        Timber.d(message = "${this::class.simpleName} onResume")
        (activity as? MainActivity)?.setNavigationVisible(isNavigationVisible)
        // Setup Keyboard Listener
        toggleKeyboardListener(isOpen = useKeyboardListener)
        // Setup OnBackPressed
        if (onBackPressedCallback == null) {
            onBackPressedCallback = object : OnBackPressedCallback(!useActivityOnBackPressed) {
                override fun handleOnBackPressed() {
                    binding.handleOnBackPressed()
                }
            }
            activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback!!)
        }
        // Setup Device Orientation
        if (lockedDeviceOrientation) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.d(message = "${this::class.simpleName} onPause")
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        toggleKeyboardListener(isOpen = false)
        if (lockedDeviceOrientation) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d(message = "${this::class.simpleName} onDestroyView")
        // 再次移除，避免未刪乾淨
        onBackPressedCallback?.remove()
        onBackPressedCallback = null

        // 強制關閉 Loading Dialog
        navigateLoadingDialog(false)

        binding.destroy()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d(message = "${this::class.simpleName} onDestroy")
    }

    // region private function
    /**
     * 設定鍵盤開關監聽器
     *
     * @param isOpen [Boolean] 是否開啟監聽器
     */
    private fun toggleKeyboardListener(isOpen: Boolean) {
        if (isOpen) {
            @Suppress("DEPRECATION")
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
                    view.setPadding(0, 0, 0, insets.bottom)
                    WindowInsetsCompat.CONSUMED
                }
            }
        } else {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }
    }
    // endregion private function

    // region open function
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
     * 設定 fragment or dialog 的 Callback，可在此設定 Callback。
     */
    open fun VB.setCallback() {}

    /**
     * 銷毀用的函式，可在Fragment銷毀時，同時移除不需要的事件。
     */
    open fun VB.destroy() {}

    /**
     * 複寫返回鍵
     */
    open fun VB.handleOnBackPressed() {
        popBackStack()
    }
    // endregion open function

    // region navigation
    /**
     * 返回上一頁
     *
     * 注意：盡量少用，除非已經有複寫 onBackPress 事件，否則請使用 popBackStack() 或 navigateUp()
     *
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun onBackPressed(
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        try {
            activity?.onBackPressedDispatcher?.onBackPressed()
        } catch (e: Exception) {
            Timber.e(message = "onBackPressed()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 前往指定的目標
     *
     * @param resId [Int] 指定的目標
     * @param args [Bundle] 傳遞的參數
     * @param navOptions [NavOptions]
     * @param navigatorExtras [Navigator.Extras]
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun navigate(
        @IdRes resId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.navigate(resId, args, navOptions, navigatorExtras)
        } catch (e: Exception) {
            Timber.e(message = "navigate()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 前往指定的目標
     *
     * @param directions [NavDirections] 指定的目標
     * @param navOptions [NavOptions]
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun navigate(
        directions: NavDirections,
        navOptions: NavOptions? = null,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.navigate(directions, navOptions)
        } catch (e: Exception) {
            Timber.e(message = "navigate()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 前往指定的目標
     *
     * @param directions [NavDirections] 指定的目標
     * @param navigatorExtras [Navigator.Extras]
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun navigate(
        directions: NavDirections,
        navigatorExtras: Navigator.Extras,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.navigate(directions, navigatorExtras)
        } catch (e: Exception) {
            Timber.e(message = "navigate()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 前往指定的目標，使用 deepLink
     *
     * @param deepLink [String] 指定的目標
     * @param navOptions [NavOptions]
     * @param navigatorExtras [Navigator.Extras]
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun navigate(
        deepLink: Uri,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.navigate(deepLink, navOptions, navigatorExtras)
        } catch (e: Exception) {
            Timber.e(message = "navigate()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 前往指定的目標，使用 deepLink
     *
     * @param request [NavDeepLinkRequest] 指定的目標
     * @param navOptions [NavOptions]
     * @param navigatorExtras [Navigator.Extras]
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun navigate(
        request: NavDeepLinkRequest,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.navigate(request, navOptions, navigatorExtras)
        } catch (e: Exception) {
            Timber.e(message = "navigate()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 移除當前的 Fragment
     *
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun popBackStack(
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.popBackStack()
        } catch (e: Exception) {
            Timber.e(message = "popBackStack()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 移除當前的 Fragment
     *
     * @param destinationId [Int] 目標 ID
     * @param inclusive [Boolean] 是否包含當前的 Fragment
     * @param saveState [Boolean] 是否儲存狀態
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun popBackStack(
        @IdRes destinationId: Int,
        inclusive: Boolean,
        saveState: Boolean = false,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.popBackStack(destinationId, inclusive, saveState)
        } catch (e: Exception) {
            Timber.e(message = "popBackStack()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 移除當前的 Fragment
     *
     * @param route [String] 路徑
     * @param inclusive [Boolean] 是否包含當前的 Fragment
     * @param saveState [Boolean] 是否儲存狀態
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean = false,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.popBackStack(route, inclusive, saveState)
        } catch (e: Exception) {
            Timber.e(message = "popBackStack()", t = e)
            errorCallback?.invoke(e)
        }
    }

    /**
     * 移除當前的 Fragment ，遵循 destinationId 的路徑。
     *
     * @param errorCallback [Function] 錯誤回調
     */
    protected fun navigateUp(
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        if (!isAdded) return
        try {
            navController.navigateUp()
        } catch (e: Exception) {
            Timber.e(message = "navigate()", t = e)
            errorCallback?.invoke(e)
        }
    }
    // endregion navigation
}