/**
 * P0-02 ～ P0-06：地圖驗證
 *
 * 結構採用 AGP 9 之後的 KMP 標準：
 * - shared：共用程式碼與 UI（Kotlin Multiplatform + Compose Multiplatform）
 * - androidApp：Android 進入點（只套用 com.android.application）
 * - iosApp：iOS 進入點（Xcode 專案，以 XcodeGen 產生）
 */
plugins {
    id("com.android.application") version "9.5.2" apply false
    id("com.android.kotlin.multiplatform.library") version "9.5.2" apply false
    kotlin("multiplatform") version "2.4.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
    id("org.jetbrains.compose") version "1.12.2" apply false
}
