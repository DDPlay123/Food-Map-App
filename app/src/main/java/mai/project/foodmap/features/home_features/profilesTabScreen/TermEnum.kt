package mai.project.foodmap.features.home_features.profilesTabScreen

import androidx.annotation.StringRes
import mai.project.foodmap.R

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
}