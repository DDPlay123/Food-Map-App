package mai.project.foodmap

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.isScreenLocked
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.launchWithoutRepeat
import mai.project.core.extensions.showSnackBar
import mai.project.core.utils.Event
import mai.project.foodmap.base.BaseActivity
import mai.project.foodmap.databinding.ActivityMainBinding
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.utils.handleResult
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, SharedViewModel>(
    bindingInflater = ActivityMainBinding::inflate
) {
    override val viewModel by viewModels<SharedViewModel>()

    /**
     * 導航控制器
     */
    private lateinit var navController: NavController

    /**
     * 導航基底 Fragment
     */
    private lateinit var navHostFragment: NavHostFragment

    /**
     * 記錄返回鍵按下時間
     */
    private var backPressedTime: Long = 0

    /**
     * 暫存登入狀態的導航功能
     *
     * - 在背景啟動 App 的狀況下，會導航失敗，所以延後執行
     */
    private var pendingLoginNavigation: (() -> Unit)? = null

    /**
     * 顯示 SnackBar (主要是避免 SnackBar 被 BottomNavigationView 遮住)
     *
     * @param message 訊息
     * @param actionText 按鈕文字 (不顯示則為空字串)
     * @param duration 顯示時間
     * @param action 點擊按鈕後要做的事情
     */
    fun showSnackBar(
        message: String,
        actionText: String = "",
        duration: Int = Snackbar.LENGTH_SHORT,
        action: ((Snackbar) -> Unit)? = null,
    ) = with(binding) {
        root.showSnackBar(
            message = message,
            actionText = actionText,
            duration = duration,
            anchorView = if (bottomNavigation?.isVisible == true) bottomNavigation else null,
            doSomething = action
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setBackPress()
        doInitialization()
        setListener()
        setObserver()

        savedInstanceState?.let { bundle ->
            val showNavigationView = bundle.getBoolean(KEY_SHOW_NAVIGATION_VIEW)
            binding.bottomNavigation?.isVisible = showNavigationView
            binding.sideNavigation?.isVisible = showNavigationView
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val showNavigationView =
            binding.bottomNavigation?.isVisible == true || binding.sideNavigation?.isVisible == true
        outState.putBoolean(KEY_SHOW_NAVIGATION_VIEW, showNavigationView)
    }

    override fun onResume() {
        super.onResume()
        pendingLoginNavigation?.invoke()
    }

    /**
     * 設定返回鍵事件
     *
     * - 讓使用者在 3 秒內按下兩次返回鍵才能退出程式
     */
    private fun setBackPress() {
        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedTime + 3000 > System.currentTimeMillis()) {
                        finish()
                    } else {
                        showSnackBar(getString(R.string.sentence_again_to_exit))
                    }

                    backPressedTime = System.currentTimeMillis()
                }
            }
        )
    }

    /**
     * 初始化
     */
    private fun doInitialization() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation?.let { NavigationUI.setupWithNavController(it, navController) }
        binding.sideNavigation?.let { NavigationUI.setupWithNavController(it, navController) }
    }

    /**
     * 設定監聽器
     */
    private fun setListener() = with(binding) {
        bottomNavigation?.setOnItemReselectedListener { /* 不做事 */ }
        sideNavigation?.setOnItemReselectedListener { /* 不做事 */ }
    }

    /**
     * 設定觀察者
     */
    private fun setObserver() = with(viewModel) {
        // 單次事件 (不需要配合生命週期循環搜集)
        launchWithoutRepeat(
            // 登入/登出 切換
            { isLogin.collect(::handleIsLoginState) },
            // 顯示模式 切換
            { themeMode.collect(AppCompatDelegate::setDefaultNightMode) },
            // 語言模式 切換
            { languageMode.collect(::setAppLanguage) }
        )
        // 其他事件
        launchAndRepeatStarted(
            // 如果 AccessKey 發生錯誤，則強制登出
            { GlobalEvent.accessKeyIllegal.collect(::handleAccessKeyIllegalState) },
            // 新增 FCM Token 狀態
            { addFcmTokenResult.collect(::handleBasicResult) },
            // 取得 使用者大頭貼 狀態
            { getUserImageResult.collect(::handleBasicResult) }
        )
    }

    /**
     * 處理是否已登入的狀態
     */
    private fun handleIsLoginState(isLogin: Boolean) = with(binding) {
        Timber.d(message = "當前登入狀態：$isLogin")
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .setPopUpTo(R.id.nav_main, true)
            .build()

        val navigateAction = if (isLogin) {
            viewModel.addFcmToken()
            viewModel.getUserImage()
            NavMainDirections.actionGlobalToHomeTabFragment()
        } else {
            NavMainDirections.actionGlobalToIntroductionFragment(true)
        }

        if (isScreenLocked) {
            pendingLoginNavigation = { safeNavigate(navigateAction, navOptions) }
        } else {
            pendingLoginNavigation = null
            safeNavigate(navigateAction, navOptions)
        }

        bottomNavigation?.isVisible = isLogin
        sideNavigation?.isVisible = isLogin
    }

    /**
     * 處理 AccessKey 錯誤的狀態
     */
    private suspend fun handleAccessKeyIllegalState(event: Event<Boolean>) {
        val isError = event.getContentIfNotHandled ?: return
        if (isError) {
            viewModel.clearAllData()
            withContext(Dispatchers.Main.immediate) {
                displayToast(getString(R.string.sentence_login_expired))
            }
        }
    }

    /**
     * 處理 API 基礎回傳結果
     *
     * @param event API 回傳事件
     */
    private fun <T> handleBasicResult(
        event: Event<NetworkResult<T>>
    ) = with(viewModel) {
        event.getContentIfNotHandled?.handleResult {
            onLoading = { setLoading(true) }
            onSuccess = { setLoading(false) }
            onError = { _, msg ->
                setLoading(false)
                displayToast(msg ?: "Unknown Error")
            }
        }
    }

    /**
     * 安全導航，避免發生錯誤
     *
     * @param directions 導航方向
     * @param navOptions 導航選項
     */
    private fun safeNavigate(
        directions: NavDirections,
        navOptions: NavOptions? = null,
    ) {
        try {
            navController.navigate(directions, navOptions)
        } catch (e: Exception) {
            Timber.e(message = "safeNavigate", t = e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    companion object {
        /**
         * 當重建 Activity (畫面旋轉、螢幕縮放等...) 時，判斷是否要顯示 NavigationView
         */
        private const val KEY_SHOW_NAVIGATION_VIEW = "KEY_SHOW_NAVIGATION_VIEW"
    }
}