import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * Maps API 金鑰：優先讀 local.properties 的 MAPS_API_KEY，其次讀環境變數 GOOGLE_API_KEY。
 * 正式 App 的金鑰若限制了套件名稱，需要把 mai.project.foodmap.spike.map 加進允許清單，
 * 或改用一把開發用金鑰。
 */
val mapsApiKey: String = run {
    val props = Properties()
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(props::load)
    (props.getProperty("MAPS_API_KEY") ?: System.getenv("GOOGLE_API_KEY") ?: "")
        .trim()
        .removeSurrounding("\"")
}

android {
    namespace = "mai.project.foodmap.spike.map"
    compileSdk = 37

    defaultConfig {
        applicationId = "mai.project.foodmap.spike.map"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.13.0")
}
