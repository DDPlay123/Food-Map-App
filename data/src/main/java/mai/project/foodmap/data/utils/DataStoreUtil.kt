package mai.project.foodmap.data.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

/**
 * DataStore 工具類
 *
 * @property dataStore DataStore Context
 * @property clearAll 清空所有資料
 * @property putData 存入資料
 * @property getData 取得資料
 */
internal object DataStoreUtil {

    /**
     * DataStore Context
     */
    internal val Context.dataStore by preferencesDataStore(
        name = "MyPreferences"
    )

    /**
     * Clear All DataStore
     */
    suspend fun DataStore<Preferences>.clearAll() = edit { it.clear() }

    /**
     * PUT Any Variable
     */
    suspend inline fun <reified T> DataStore<Preferences>.putData(key: String, value: T) =
        edit {
            when (T::class) {
                Int::class -> it[intPreferencesKey(key)] = value as Int
                Long::class -> it[longPreferencesKey(key)] = value as Long
                // 針對 String 做加密，如果失敗就直接存入
                String::class -> it[stringPreferencesKey(key)] =
                    AES.encrypt(message = value as String) ?: value as String

                Boolean::class -> it[booleanPreferencesKey(key)] = value as Boolean
                Float::class -> it[floatPreferencesKey(key)] = value as Float
                Double::class -> it[doublePreferencesKey(key)] = value as Double
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }

    /**
     * GET Any Variable
     */
    inline fun <reified T> DataStore<Preferences>.getData(key: String, default: T): Flow<T> {
        return data.catch { exception ->
            if (exception is IOException) {
                Timber.e(message = "Error get data", t = exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            when (T::class) {
                Int::class -> (it[intPreferencesKey(key)] ?: default) as T
                Long::class -> (it[longPreferencesKey(key)] ?: default) as T
                // 針對 String 做解密，如果失敗就清空資料
                String::class -> {
                    val text = it[stringPreferencesKey(key)]
                    if (text == null) {
                        // 如果解密失敗就清空資料
                        Timber.e(message = "key：$key\nDataStore String is Null. So return default value.")
                        return@map default
                    }
                    AES.decrypt(base64EncodedCipherText = text) as T
                }

                Boolean::class -> (it[booleanPreferencesKey(key)] ?: default) as T
                Float::class -> (it[floatPreferencesKey(key)] ?: default) as T
                Double::class -> (it[doublePreferencesKey(key)] ?: default) as T
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }
}