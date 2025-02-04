plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.navigation.safeArgs)
    alias(libs.plugins.google.services)
    alias(libs.plugins.mapsPlatform)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "mai.project.foodmap"

    /**
     * 設定預設設定檔
     */
    defaultConfig {
        applicationId = "mai.project.foodmap"
        versionCode = 1
        versionName = "1.0.0"

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