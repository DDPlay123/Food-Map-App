plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.mapsPlatform)
}

android {
    namespace = "mai.project.core"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    // Android X
    implementation(libs.browser)

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
}