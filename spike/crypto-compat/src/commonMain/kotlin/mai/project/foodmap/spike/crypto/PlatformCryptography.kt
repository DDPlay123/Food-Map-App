package mai.project.foodmap.spike.crypto

import dev.whyoleg.cryptography.CryptographyProvider

/**
 * 各平台使用的加密實作：JVM / Android 為 JDK（javax.crypto），iOS 為 Apple CommonCrypto。
 *
 * 明確指定 provider，而不是依賴自動註冊，確保兩個平台跑的是預期的原生實作。
 */
internal expect val platformCryptographyProvider: CryptographyProvider
