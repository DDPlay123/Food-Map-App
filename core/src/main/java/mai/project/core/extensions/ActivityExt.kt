package mai.project.core.extensions

import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 建立 LifecycleScope 並在 Activity 的生命週期為 STARTED 時重複執行
 *
 * @param launchBlock 要執行的區塊
 */
fun AppCompatActivity.launchAndRepeatStarted(
    vararg launchBlock: suspend () -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launchBlock.forEach {
                launch { it.invoke() }
            }
        }
    }
}

/**
 * 建立 LifecycleScope，不受 Activity/Fragment 的生命週期控制
 *
 * @param launchBlock 要執行的區塊
 */
fun AppCompatActivity.launchWithoutRepeat(
    vararg launchBlock: suspend () -> Unit
) {
    lifecycleScope.launch {
        launchBlock.forEach {
            launch { it.invoke() }
        }
    }
}

/**
 * 啟動 App 設定頁面
 */
fun AppCompatActivity.openAppSettings() {
    try {
        with(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) {
            data = "package:${packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    } catch (e: Exception) {
        Timber.e(message = "openAppSettings() 發生錯誤", t = e)
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

/**
 * 啟動 App GPS 設定頁面
 */
fun AppCompatActivity.openGpsSettings() {
    try {
        with(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    } catch (e: Exception) {
        Timber.e(message = "openGpsSettings() 發生錯誤", t = e)
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}