import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.navigation.safeArgs) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.mapsPlatform) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.android")
    apply(plugin = "com.google.dagger.hilt.android")

    afterEvaluate {
        extensions.findByType<com.android.build.gradle.BaseExtension>()?.apply {
            compileSdkVersion(35)
            defaultConfig {
                minSdk = 24
                targetSdk = 35
            }

            // 設定 Java 編譯版本
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }

        // 設定 Kotlin 編譯版本
        extensions.findByType<KotlinJvmProjectExtension>()?.apply {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
