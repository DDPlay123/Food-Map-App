package mai.project.foodmap.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import timber.log.Timber

/**
 * 基礎 Dialog ，用於繼承
 */
abstract class BaseDialog<VB : ViewBinding, VM : BaseViewModel>(
    private val bindingInflater: (LayoutInflater) -> VB
) : DialogFragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    protected open val viewModel: VM? = null

    /**
     * 取得 NavController
     */
    protected lateinit var navController: NavController
        private set

    /**
     * 是否使用全螢幕
     */
    protected open val useFullScreen: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Timber.d(message = "${this::class.simpleName} onCreateView")
        _binding = bindingInflater.invoke(inflater)
        // 設定背景透明
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d(message = "${this::class.simpleName} onViewCreated")
        navController = findNavController()
        setFullScreenEnabled(useFullScreen)

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

    // region private function
    /**
     * 設定全螢幕
     *
     * @param enabled 是否全螢幕
     */
    private fun setFullScreenEnabled(enabled: Boolean) {
        val window = dialog?.window
        val params = window?.attributes

        if (enabled) {
            params?.width = ViewGroup.LayoutParams.MATCH_PARENT
            params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            params?.width = ViewGroup.LayoutParams.WRAP_CONTENT
            params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        window?.attributes = params
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
     * 設定 Callback 用的函式，可在此設定 Callback。
     */
    open fun VB.setCallback() {}

    /**
     * 銷毀用的函式，可在Fragment銷毀時，同時移除不需要的事件。
     */
    open fun VB.destroy() {}
    // endregion open function
}