package mai.project.foodmap.data.repositoryImpl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import mai.project.foodmap.data.utils.DataStoreUtil.clearAll
import mai.project.foodmap.data.utils.DataStoreUtil.dataStore
import mai.project.foodmap.data.utils.DataStoreUtil.getData
import mai.project.foodmap.data.utils.DataStoreUtil.putData
import mai.project.foodmap.domain.repository.PreferenceRepo
import javax.inject.Inject

internal class PreferenceRepoImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
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

    override suspend fun writePassword(password: String) {
        dataStore.putData(PREF_PASSWORD, password)
    }

    override val readPassword: Flow<String>
        get() = dataStore.getData(PREF_PASSWORD, "")

    override suspend fun writeFcmToken(fcmToken: String) {
        dataStore.putData(PREF_FCM_TOKEN, fcmToken)
    }

    override val readFcmToken: Flow<String>
        get() = dataStore.getData(PREF_FCM_TOKEN, "")

    private companion object {
        const val PREF_FID = "PREF_FID"
        const val PREF_USER_ID = "PREF_USER_ID"
        const val PREF_ACCESS_KEY = "PREF_ACCESS_KEY"
        const val PREF_USERNAME = "PREF_USERNAME"
        const val PREF_PASSWORD = "PREF_PASSWORD"
        const val PREF_FCM_TOKEN = "PREF_FCM_TOKEN"
    }
}