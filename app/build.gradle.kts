import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.navigation.safeArgs)
    alias(libs.plugins.google.services)
    alias(libs.plugins.mapsPlatform)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "mai.project.foodmap"

    /**
     * 設定預設設定檔
     */
    defaultConfig {
        applicationId = "mai.project.foodmap"
        versionCode = 2
        versionName = "1.0.0"

        buildConfigField("String", "GOOGLE_API_KEY", localProperties.getProperty("GOOGLE_API_KEY"))
        manifestPlaceholders["GOOGLE_API_KEY"] = localProperties.getProperty("GOOGLE_API_KEY")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /**
     * 設定簽名檔
     */
    signingConfigs {
        getByName("debug") {
            storeFile = file(localProperties.getProperty("DEBUG_STORE_FILE"))
            storePassword = localProperties.getProperty("STORE_PASSWORD") as String
            keyAlias = localProperties.getProperty("KEY_ALIAS") as String
            keyPassword = localProperties.getProperty("KEY_PASSWORD") as String
        }

        create("release") {
            storeFile = file(localProperties.getProperty("RELEASE_STORE_FILE"))
            storePassword = localProperties.getProperty("STORE_PASSWORD") as String
            keyAlias = localProperties.getProperty("KEY_ALIAS") as String
            keyPassword = localProperties.getProperty("KEY_PASSWORD") as String
        }
    }

    /**
     * 設定建構類型 分為 debug 與 release
     */
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
    }

    /**
     * 設定 Kotlin 編譯版本
     */
    kotlinOptions { jvmTarget = "17" }

    /**
     * 設定配置打包時排除的檔案
     */
    packaging {
        resources {
            excludes.add("project.properties")
            excludes.add("META-INF/INDEX.LIST")
        }
    }

    /**
     * 設定 Android 建構屬性
     */
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    // Android X
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.bundles.lifecycle)
    implementation(libs.paging.runtime.ktx)

    // ksp
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)

    // Google Library
    implementation(libs.material)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.fragment)
    implementation(libs.bundles.googleMap)

    // Firebase Library
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // 3rd Party Library
    implementation(libs.timber)
    implementation(libs.bundles.coil)
    implementation(libs.lottie)
    implementation(libs.geolib.polyline)
    implementation(libs.image.crop)
}