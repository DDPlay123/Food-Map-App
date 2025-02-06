import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "mai.project.foodmap.data"

    defaultConfig {
        buildConfigField("String", "API_KEY", localProperties.getProperty("BASE_URL"))
        buildConfigField("String", "AES_KEY", localProperties.getProperty("AES_KEY"))

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

    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(project(":domain"))

    // Android X
    implementation(libs.bundles.room)
    implementation(libs.datastore.preferences)
    implementation(libs.security.crypto)

    // ksp
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)

    // Google Library
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.fragment)

    // Firebase Library
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // 3rd Party Library
    implementation(libs.timber)
    implementation(libs.bundles.retrofit)
    implementation(libs.serialization)
}