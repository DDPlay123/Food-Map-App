plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.navigation.safeArgs)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.mapsPlatform)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.crashlytics)
}

val mCompileSdk = libs.versions.compileSdk.get().toInt()
val mMinSdk = libs.versions.minSdk.get().toInt()
val mTargetSdk = libs.versions.targetSdk.get().toInt()
// 版本號
val mVersionCode = 1
// 版本名稱
val mVersionName = "1.0.0"

android {
    namespace = "mai.project.foodmap"
    compileSdk = mCompileSdk

    /**
     * 設定預設設定檔
     */
    defaultConfig {
        applicationId = "mai.project.foodmap"
        minSdk = mMinSdk
        targetSdk = mTargetSdk
        versionCode = mVersionCode
        versionName = mVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /**
     * 設定簽名檔
     */
    signingConfigs {
        getByName("debug") {

        }

        create("release") {

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
        }
    }

    /**
     * 設定 Java 編譯版本
     */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    /**
     * 設定 Kotlin 編譯版本
     */
    kotlinOptions {
        jvmTarget = "17"
    }

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
        viewBinding = true
    }
}

dependencies {

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

    // kapt
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
}