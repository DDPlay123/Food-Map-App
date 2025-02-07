package mai.project.foodmap.features.home_features.profilesTabScreen

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.utils.ImageLoaderUtil
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.BuildConfig
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentProfilesTabBinding
import mai.project.foodmap.features.home_features.profilesTabScreen.adapter.PersonalDataAdapter
import mai.project.foodmap.features.home_features.profilesTabScreen.adapter.SettingsLabelAdapter
import mai.project.foodmap.features.home_features.profilesTabScreen.adapter.SettingsLabelTermAdapter
import javax.inject.Inject

@AndroidEntryPoint
class ProfilesTabFragment : BaseFragment<FragmentProfilesTabBinding, ProfilesTabViewModel>(
    bindingInflater = FragmentProfilesTabBinding::inflate
) {
    override val viewModel by viewModels<ProfilesTabViewModel>()

    override val useActivityOnBackPressed: Boolean = true

    @Inject
    lateinit var imageLoaderUtil: ImageLoaderUtil

    private val personalDataAdapter by lazy { PersonalDataAdapter(imageLoaderUtil) }

    private val settingsLabelAdapter by lazy { SettingsLabelAdapter() }

    private val concatAdapter by lazy { ConcatAdapter(personalDataAdapter, settingsLabelAdapter) }

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
            settingsLabelAdapter.submitList(getSettingsLabelList())
        }
    }

    override fun FragmentProfilesTabBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 使用者大頭貼 和 使用者名稱
            { userImage.combine(userName) { p0, p1 -> p0 to p1 }.collect { handleUserInfo(it.first, it.second) } },
            // 登出狀態
            { logoutResult.collect(::handleBasicResult) }
        )
    }

    override fun FragmentProfilesTabBinding.setListener() {
        settingsLabelAdapter.onItemClick = { model ->
            when (model.id) {
                TermEnum.THEMES_TOPIC.name -> {
                    // TODO 切換顯示模式
                }

                TermEnum.DISPLAY_LANGUAGE.name -> {
                    // TODO 切換顯示語言
                }

                TermEnum.BLACK_LIST.name -> {
                    // TODO 查看黑名單列表
                }

                TermEnum.RESET_PASSWORD.name -> {
                    // TODO 重設密碼
                }

                TermEnum.DELETE_ACCOUNT.name -> {
                    // TODO 刪除帳號
                }

                TermEnum.TERMS_OF_SERVICE.name -> {
                    // TODO 查看服務條款
                }

                TermEnum.PRIVACY_POLICY.name -> {
                    // TODO 查看隱私權政策
                }

                TermEnum.LOGOUT.name -> viewModel.logout()

                else -> Unit
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
     * 取得設定資料集
     */
    private fun getSettingsLabelList(
        // TODO 待動態添加參數
        theme: String = "根據系統設定",
        language: String = "根據系統設定"
    ): List<SettingsLabelAdapter.Model> {
        return listOf(
            SettingsLabelAdapter.Model(
                title = getString(R.string.sentence_display_settings),
                terms = listOf(
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.THEMES_TOPIC.name,
                        name = getString(TermEnum.THEMES_TOPIC.stringRes),
                        subName = theme
                    ),
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.DISPLAY_LANGUAGE.name,
                        name = getString(TermEnum.DISPLAY_LANGUAGE.stringRes),
                        subName = language
                    )
                )
            ),
            SettingsLabelAdapter.Model(
                title = getString(R.string.sentence_user_settings),
                terms = listOf(
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.BLACK_LIST.name,
                        name = getString(TermEnum.BLACK_LIST.stringRes)
                    ),
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.RESET_PASSWORD.name,
                        name = getString(TermEnum.RESET_PASSWORD.stringRes)
                    ),
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.DELETE_ACCOUNT.name,
                        name = getString(TermEnum.DELETE_ACCOUNT.stringRes)
                    )
                )
            ),
            SettingsLabelAdapter.Model(
                title = getString(R.string.word_other),
                terms = listOf(
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.TERMS_OF_SERVICE.name,
                        name = getString(TermEnum.TERMS_OF_SERVICE.stringRes)
                    ),
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.PRIVACY_POLICY.name,
                        name = getString(TermEnum.PRIVACY_POLICY.stringRes)
                    ),
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.APPLICATION_VERSION.name,
                        name = getString(TermEnum.APPLICATION_VERSION.stringRes),
                        subName = "${BuildConfig.VERSION_NAME} b${BuildConfig.VERSION_CODE}"
                    ),
                    SettingsLabelTermAdapter.Model(
                        id = TermEnum.LOGOUT.name,
                        name = getString(TermEnum.LOGOUT.stringRes)
                    )
                )
            )
        )
    }
}