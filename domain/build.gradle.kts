plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "mai.project.foodmap.domain"

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
}

dependencies {

    // ksp
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)

    // Google Library
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.fragment)

    // Firebase Library
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // 3rd Party Library
    implementation(libs.timber)
}