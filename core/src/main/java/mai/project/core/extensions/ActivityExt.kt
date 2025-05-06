package mai.project.core.extensions

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import mai.project.core.annotations.NavigationMode
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
fun Activity.openAppSettings() {
    try {
        with(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) {
            data = "package:${packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    } catch (e: Exception) {
        Timber.e(t = e, message = "openAppSettings() 發生錯誤")
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

/**
 * 啟動 App GPS 設定頁面
 */
fun Activity.openGpsSettings() {
    try {
        with(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    } catch (e: Exception) {
        Timber.e(t = e, message = "openGpsSettings() 發生錯誤")
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

/**
 * 啟動電話撥打功能
 */
fun Activity.openPhoneCall(phone: String) {
    try {
        with(Intent(Intent.ACTION_DIAL)) {
            data = "tel:$phone".toUri()
            startActivity(this)
        }
    } catch (e: Exception) {
        Timber.e(t = e, message = "openPhoneCall() 發生錯誤")
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

/**
 * 開啟 URL，使用瀏覽器
 *
 * @param url 網址
 */
fun Activity.openUrlWithBrowser(url: String) {
    val formattedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "https://$url"
    }
    try {
        CustomTabsIntent.Builder().apply {
            setShowTitle(true)
            build().also {
                it.intent.putExtra(Intent.EXTRA_REFERRER, "android-app://$packageName".toUri())
                it.launchUrl(this@openUrlWithBrowser, formattedUrl.toUri())
            }
        }
    } catch (e: Exception) {
        Timber.e(t = e, message = "openUrlWithBrowser() 發生錯誤")
        FirebaseCrashlytics.getInstance().recordException(e)
        openUrl(formattedUrl)
    }
}

/**
 * 開啟 URL，使用內建瀏覽器
 *
 * @param url 檔案網址
 */
fun Activity.openUrl(url: String) {
    val formattedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "https://$url"
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = formattedUrl.toUri()
        }
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(t = e, message = "openUrl() 發生錯誤")
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

/**
 * 開啟 Google Map 導航模式
 *
 * @param mode Google Map 導航模式
 * @param latLng 經緯度
 */
fun Activity.openGoogleNavigation(
    @NavigationMode mode: String,
    latLng: LatLng
) {
    try {
        with(Intent(Intent.ACTION_VIEW)) {
            data = "google.navigation:q=${latLng.latitude},${latLng.longitude}&mode=$mode".toUri()
            `package` = "com.google.android.apps.maps"
            startActivity(this)
        }
    } catch (e: Exception) {
        Timber.e(t = e, message = "openGoogleNavigation() 發生錯誤")
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

/**
 * 分享連結
 *
 * @param title 分享的標題
 * @param shareLink 分享連結
 */
fun Activity.shareLink(
    title: String,
    shareLink: String
) {
    try {
        val share = Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, shareLink)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "text/plain"
            }
        , title)
        startActivity(share)
    } catch (e: Exception) {
        Timber.e(t = e, message = "shareLink")
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}