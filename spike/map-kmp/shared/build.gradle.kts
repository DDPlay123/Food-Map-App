plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

val composeVersion = "1.12.2"
val material3Version = "1.9.0"
val mapsComposeVersion = "9.0.1"

kotlin {
    android {
        namespace = "mai.project.foodmap.spike.map.shared"
        compileSdk = 37
        minSdk = 24
        withHostTest {}
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
            implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
            implementation("org.jetbrains.compose.ui:ui:$composeVersion")
            implementation("org.jetbrains.compose.material3:material3:$material3Version")
        }
        androidMain.dependencies {
            implementation("com.google.maps.android:maps-compose:$mapsComposeVersion")
            implementation("com.google.maps.android:maps-compose-utils:$mapsComposeVersion")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
