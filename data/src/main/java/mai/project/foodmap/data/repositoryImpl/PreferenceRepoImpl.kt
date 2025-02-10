package mai.project.foodmap.data.repositoryImpl

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import mai.project.foodmap.data.annotations.LanguageMode
import mai.project.foodmap.data.annotations.ThemeMode
import mai.project.foodmap.data.utils.DataStoreUtil.clearAll
import mai.project.foodmap.data.utils.DataStoreUtil.dataStore
import mai.project.foodmap.data.utils.DataStoreUtil.getData
import mai.project.foodmap.data.utils.DataStoreUtil.putData
import mai.project.foodmap.domain.repository.PreferenceRepo
import javax.inject.Inject

internal class PreferenceRepoImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) : PreferenceRepo {

    private val dataStore = context.dataStore

    override suspend fun clearAll() {
        dataStore.clearAll()
    }

    override suspend fun writeFid(fid: String) {
        dataStore.putData(PREF_FID, fid)
    }

    override val readFid: Flow<String>
        get() = dataStore.getData(PREF_FID, "")

    override suspend fun writeUserId(userId: String) {
        dataStore.putData(PREF_USER_ID, userId)
    }

    override val readUserId: Flow<String>
        get() = dataStore.getData(PREF_USER_ID, "")

    override suspend fun writeAccessKey(accessKey: String) {
        dataStore.putData(PREF_ACCESS_KEY, accessKey)
    }

    override val readAccessKey: Flow<String>
        get() = dataStore.getData(PREF_ACCESS_KEY, "")

    override suspend fun writeUsername(username: String) {
        dataStore.putData(PREF_USERNAME, username)
    }

    override val readUsername: Flow<String>
        get() = dataStore.getData(PREF_USERNAME, "")

    override fun writeAccount(account: String) {
        sharedPreferences.edit().putString(PREF_ACCOUNT, account).apply()
    }

    override val readAccount: String
        get() = sharedPreferences.getString(PREF_ACCOUNT, "").orEmpty()

    override fun writePassword(password: String) {
        sharedPreferences.edit().putString(PREF_PASSWORD, password).apply()
    }

    override val readPassword: String
        get() = sharedPreferences.getString(PREF_PASSWORD, "").orEmpty()

    override suspend fun writeUserImage(userImage: String) {
        dataStore.putData(PREF_USER_IMAGE, userImage)
    }

    override val readUserImage: Flow<String>
        get() = dataStore.getData(PREF_USER_IMAGE, "")

    override suspend fun writeThemeMode(@ThemeMode theme: Int) {
        dataStore.putData(PREF_THEME_MODE, theme)
    }

    override val readThemeMode: Flow<Int>
        get() = dataStore.getData(PREF_THEME_MODE, ThemeMode.SYSTEM)

    override suspend fun writeLanguageMode(@LanguageMode language: String) {
        dataStore.putData(PREF_LANGUAGE_MODE, language)
    }

    override val readLanguageMode: Flow<String>
        get() = dataStore.getData(PREF_LANGUAGE_MODE, LanguageMode.SYSTEM)

    override suspend fun writeMyPlaceId(placeId: String) {
        dataStore.putData(PREF_MY_PLACE_ID, placeId)
    }

    override val readMyPlaceId: Flow<String>
        get() = dataStore.getData(PREF_MY_PLACE_ID, "")

    private companion object {
        const val PREF_FID = "PREF_FID"
        const val PREF_USER_ID = "PREF_USER_ID"
        const val PREF_ACCESS_KEY = "PREF_ACCESS_KEY"
        const val PREF_USERNAME = "PREF_USERNAME"
        const val PREF_ACCOUNT = "PREF_ACCOUNT"
        const val PREF_PASSWORD = "PREF_PASSWORD"
        const val PREF_USER_IMAGE = "PREF_USER_IMAGE"
        const val PREF_THEME_MODE = "PREF_THEME_MODE"
        const val PREF_LANGUAGE_MODE = "PREF_LANGUAGE_MODE"
        const val PREF_MY_PLACE_ID = "PREF_MY_PLACE_ID"
    }
}