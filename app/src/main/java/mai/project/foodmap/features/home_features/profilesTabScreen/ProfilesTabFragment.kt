package mai.project.foodmap.features.home_features.profilesTabScreen

import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import mai.project.core.Configs
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.parcelable
import mai.project.core.utils.ImageLoaderUtil
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.data.annotations.LanguageMode
import mai.project.foodmap.data.annotations.ThemeMode
import mai.project.foodmap.databinding.FragmentProfilesTabBinding
import mai.project.foodmap.features.dialogs_features.prompt.PromptCallback
import mai.project.foodmap.features.dialogs_features.selector.SelectorCallback
import mai.project.foodmap.features.dialogs_features.selector.SelectorModel
import mai.project.foodmap.features.home_features.profilesTabScreen.adapter.PersonalDataAdapter
import mai.project.foodmap.features.home_features.profilesTabScreen.adapter.SettingsLabelAdapter
import javax.inject.Inject

@AndroidEntryPoint
class ProfilesTabFragment : BaseFragment<FragmentProfilesTabBinding, ProfilesTabViewModel>(
    bindingInflater = FragmentProfilesTabBinding::inflate
) {
    override val viewModel by hiltNavGraphViewModels<ProfilesTabViewModel>(R.id.nav_main)

    override val useActivityOnBackPressed: Boolean = true

    @Inject
    lateinit var imageLoaderUtil: ImageLoaderUtil

    private val personalDataAdapter by lazy { PersonalDataAdapter(imageLoaderUtil) }

    private val settingsLabelAdapter by lazy { SettingsLabelAdapter() }

    private val concatAdapter by lazy { ConcatAdapter(personalDataAdapter, settingsLabelAdapter) }

    private val themeModeItems: List<SelectorModel> by lazy {
        resources.getStringArray(R.array.theme_mode).mapIndexed { index, s ->
            SelectorModel(id = index, content = s)
        }
    }

    private val languageModeItems: List<SelectorModel> by lazy {
        resources.getStringArray(R.array.language_mode).mapIndexed { index, s ->
            SelectorModel(id = index, content = s)
        }
    }

    override fun FragmentProfilesTabBinding.initialize(savedInstanceState: Bundle?) {
        with(rvSettings) {
            itemAnimator = null
            addItemDecoration(
                SpacesItemDecoration(
                    direction = Direction.VERTICAL,
                    space = 10.DP,
                    endSpace = 10.DP
                )
            )
            adapter = concatAdapter
        }
    }

    override fun FragmentProfilesTabBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // Loading
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 使用者大頭貼 和 使用者名稱
            { userImage.combine(userName) { p0, p1 -> p0 to p1 }.collect { handleUserInfo(it.first, it.second) } },
            // 顯示模式 & 語言模式
            { themeMode.combine(languageMode) { p0, p1 -> p0 to p1 }.collect { handleUserSettings(it.first, it.second) } },
            // 登出狀態
            { logoutResult.collect(::handleBasicResult) },
            // 修改密碼狀態
            { resetPasswordResult.collect { handleBasicResult(it, workOnSuccess = { displayToast(getString(R.string.sentence_reset_password_success)) }) } },
            // 刪除帳號狀態
            { deleteAccountResult.collect(::handleBasicResult) }
        )
    }

    override fun FragmentProfilesTabBinding.setListener() {
        settingsLabelAdapter.onItemClick = { model ->
            when (model.id) {
                TermEnum.THEMES_TOPIC.name -> navigateSelectorDialog(
                    requestCode = REQUEST_CODE_THEME_MODE,
                    items = themeModeItems
                )

                TermEnum.DISPLAY_LANGUAGE.name -> navigateSelectorDialog(
                    requestCode = REQUEST_CODE_LANGUAGE_MODE,
                    items = languageModeItems
                )

                TermEnum.BLACK_LIST.name -> {
                    // TODO 查看黑名單列表
                }

                TermEnum.RESET_PASSWORD.name -> navigatePromptDialog(
                    requestCode = REQUEST_CODE_RESET_PASSWORD,
                    title = getString(R.string.sentence_reset_password),
                    message = getString(R.string.sentence_reset_password_prompt),
                    enableInput = true,
                    inputHint = getString(R.string.sentence_type_password_hint)
                )

                TermEnum.DELETE_ACCOUNT.name -> navigatePromptDialog(
                    requestCode = REQUEST_CODE_DELETE_ACCOUNT,
                    title = getString(R.string.sentence_delete_account),
                    message = getString(R.string.sentence_delete_account_prompt),
                    enableInput = true,
                    inputHint = getString(R.string.sentence_type_username_hint)
                )

                TermEnum.TERMS_OF_SERVICE.name -> {
                    // TODO 查看服務條款
                }

                TermEnum.PRIVACY_POLICY.name -> {
                    // TODO 查看隱私權政策
                }

                TermEnum.LOGOUT.name -> navigatePromptDialog(
                    requestCode = REQUEST_CODE_LOGOUT_HINT,
                    title = getString(R.string.word_logout),
                    message = getString(R.string.sentence_logout_prompt)
                )

                else -> Unit
            }
        }
    }

    override fun FragmentProfilesTabBinding.setCallback() {
        setFragmentResultListener(REQUEST_CODE_LOGOUT_HINT) { _, bundle ->
            bundle.parcelable<PromptCallback>(PromptCallback.ARG_CONFIRM)?.let {
                viewModel.logout()
            }
        }
        setFragmentResultListener(REQUEST_CODE_THEME_MODE) { _, bundle ->
            bundle.parcelable<SelectorCallback>(SelectorCallback.ARG_ITEM_CLICK)?.let { callback ->
                callback as SelectorCallback.OnItemClick
                when (callback.item) {
                    themeModeItems[0] -> viewModel.setThemeMode(ThemeMode.SYSTEM)
                    themeModeItems[1] -> viewModel.setThemeMode(ThemeMode.DARK)
                    themeModeItems[2] -> viewModel.setThemeMode(ThemeMode.LIGHT)
                }
            }
        }
        setFragmentResultListener(REQUEST_CODE_LANGUAGE_MODE) { _, bundle ->
            bundle.parcelable<SelectorCallback>(SelectorCallback.ARG_ITEM_CLICK)?.let { callback ->
                callback as SelectorCallback.OnItemClick
                when (callback.item) {
                    languageModeItems[0] -> viewModel.setLanguageMode(LanguageMode.SYSTEM)
                    languageModeItems[1] -> viewModel.setLanguageMode(LanguageMode.ENGLISH)
                    languageModeItems[2] -> viewModel.setLanguageMode(LanguageMode.TRADITIONAL_CHINESE)
                }
            }
        }
        setFragmentResultListener(REQUEST_CODE_RESET_PASSWORD) { _, bundle ->
            bundle.parcelable<PromptCallback>(PromptCallback.ARG_CONFIRM)?.let { callback ->
                val password = (callback as PromptCallback.OnConfirm).outputString
                when {
                    password.isEmpty() -> displayToast(getString(R.string.rule_password_empty))

                    password.length < Configs.PASSWORD_LENGTH_MIN ->
                        displayToast(getString(R.string.rule_password_length))

                    password.length > Configs.PASSWORD_LENGTH_MAX ->
                        displayToast(getString(R.string.rule_password_limit))

                    else -> viewModel.resetPassword(password)
                }
            }
        }
        setFragmentResultListener(REQUEST_CODE_DELETE_ACCOUNT) { _, bundle ->
            bundle.parcelable<PromptCallback>(PromptCallback.ARG_CONFIRM)?.let { callback ->
                callback as PromptCallback.OnConfirm
                if (callback.outputString == viewModel.userName.value) {
                    viewModel.deleteAccount()
                } else {
                    displayToast(getString(R.string.sentence_username_error))
                }
            }
        }
    }

    /**
     * 處理使用者資訊
     */
    private fun handleUserInfo(
        userImage: String,
        username: String,
    ) {
        personalDataAdapter.submitModel(userImage, username)
    }

    /**
     * 處理使用者設定
     */
    private fun handleUserSettings(
        @ThemeMode
        themeMode: Int,
        @LanguageMode
        languageMode: String
    ) {
        settingsLabelAdapter.submitList(TermEnum.getSettingsLabelList(requireContext(), themeMode, languageMode))
    }

    companion object {
        /**
         * 提示是否要登出 Dialog
         */
        private const val REQUEST_CODE_LOGOUT_HINT = "REQUEST_CODE_LOGOUT_HINT"

        /**
         * 選擇顯示模式 Dialog
         */
        private const val REQUEST_CODE_THEME_MODE = "REQUEST_CODE_THEME_MODE"

        /**
         * 選擇語言模式 Dialog
         */
        private const val REQUEST_CODE_LANGUAGE_MODE = "REQUEST_CODE_LANGUAGE_MODE"

        /**
         * 重設密碼 Dialog
         */
        private const val REQUEST_CODE_RESET_PASSWORD = "REQUEST_CODE_RESET_PASSWORD"

        /**
         * 刪除帳號提示 Dialog
         */
        private const val REQUEST_CODE_DELETE_ACCOUNT = "REQUEST_CODE_DELETE_ACCOUNT"
    }
}