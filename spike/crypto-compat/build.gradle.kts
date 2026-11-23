/**
 * P0-07 / P0-08：AES 相容性驗證
 *
 * 後端無法改動，跨平台版本送出的加密密碼必須和現有 Android App 逐位元組相同。
 * - JVM：與 javax.crypto 參考實作交叉比對，並驗證標準答案（golden vectors）
 * - iOS：在模擬器上跑同一組標準答案（需要 Mac）
 */
plugins {
    kotlin("multiplatform") version "2.4.21"
}

val cryptographyVersion = "0.6.0"

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("dev.whyoleg.cryptography:cryptography-core:$cryptographyVersion")
        }
        jvmMain.dependencies {
            implementation("dev.whyoleg.cryptography:cryptography-provider-jdk:$cryptographyVersion")
        }
        iosMain.dependencies {
            implementation("dev.whyoleg.cryptography:cryptography-provider-apple:$cryptographyVersion")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
