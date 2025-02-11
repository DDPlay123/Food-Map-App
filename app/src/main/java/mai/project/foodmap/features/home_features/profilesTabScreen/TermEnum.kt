package mai.project.foodmap.features.home_features.profilesTabScreen

import android.content.Context
import androidx.annotation.StringRes
import mai.project.foodmap.BuildConfig
import mai.project.foodmap.R
import mai.project.foodmap.data.annotations.LanguageMode
import mai.project.foodmap.data.annotations.ThemeMode
import mai.project.foodmap.features.home_features.profilesTabScreen.adapter.SettingsLabelAdapter
import mai.project.foodmap.features.home_features.profilesTabScreen.adapter.SettingsLabelTermAdapter

enum class TermEnum(
    @StringRes
    val stringRes: Int
) {
    THEMES_TOPIC(R.string.sentence_themes_topic),
    DISPLAY_LANGUAGE(R.string.sentence_display_language),
    BLACK_LIST(R.string.sentence_black_list),
    RESET_PASSWORD(R.string.sentence_reset_password),
    DELETE_ACCOUNT(R.string.sentence_delete_account),
    TERMS_OF_SERVICE(R.string.sentence_terms_of_service),
    PRIVACY_POLICY(R.string.sentence_privacy_policy),
    APPLICATION_VERSION(R.string.sentence_application_version),
    LOGOUT(R.string.word_logout);

    companion object {
        /**
         * 取得設定資料集
         */
        fun getSettingsLabelList(
            context: Context,
            @ThemeMode
            themeMode: Int,
            @LanguageMode
            languageMode: String
        ): List<SettingsLabelAdapter.Model> {
            val themeModeList = context.resources.getStringArray(R.array.theme_mode)
            val languageList = context.resources.getStringArray(R.array.language_mode)
            return listOf(
                SettingsLabelAdapter.Model(
                    title = context.getString(R.string.sentence_display_settings),
                    terms = listOf(
                        SettingsLabelTermAdapter.Model(
                            id  = THEMES_TOPIC.name,
                            name = context.getString(THEMES_TOPIC.stringRes),
                            subName = when (themeMode) {
                                ThemeMode.DARK -> themeModeList[1]
                                ThemeMode.LIGHT -> themeModeList[2]
                                else -> themeModeList[0]
                            }
                        ),
                        SettingsLabelTermAdapter.Model(
                            id = DISPLAY_LANGUAGE.name,
                            name = context.getString(DISPLAY_LANGUAGE.stringRes),
                            subName = when (languageMode) {
                                LanguageMode.ENGLISH -> languageList[1]
                                LanguageMode.TRADITIONAL_CHINESE -> languageList[2]
                                else -> languageList[0]
                            }
                        )
                    )
                ),
                SettingsLabelAdapter.Model(
                    title = context.getString(R.string.sentence_user_settings),
                    terms = listOf(
//                        SettingsLabelTermAdapter.Model(
//                            id = BLACK_LIST.name,
//                            name = context.getString(BLACK_LIST.stringRes)
//                        ),
                        SettingsLabelTermAdapter.Model(
                            id = RESET_PASSWORD.name,
                            name = context.getString(RESET_PASSWORD.stringRes)
                        ),
                        SettingsLabelTermAdapter.Model(
                            id = DELETE_ACCOUNT.name,
                            name = context.getString(DELETE_ACCOUNT.stringRes)
                        )
                    )
                ),
                SettingsLabelAdapter.Model(
                    title = context.getString(R.string.word_other),
                    terms = listOf(
                        SettingsLabelTermAdapter.Model(
                            id = TERMS_OF_SERVICE.name,
                            name = context.getString(TERMS_OF_SERVICE.stringRes)
                        ),
                        SettingsLabelTermAdapter.Model(
                            id = PRIVACY_POLICY.name,
                            name = context.getString(PRIVACY_POLICY.stringRes)
                        ),
                        SettingsLabelTermAdapter.Model(
                            id = APPLICATION_VERSION.name,
                            name = context.getString(APPLICATION_VERSION.stringRes),
                            subName = "v${BuildConfig.VERSION_NAME} b${BuildConfig.VERSION_CODE}"
                        ),
                        SettingsLabelTermAdapter.Model(
                            id = LOGOUT.name,
                            name = context.getString(LOGOUT.stringRes)
                        )
                    )
                )
            )
        }
    }
}