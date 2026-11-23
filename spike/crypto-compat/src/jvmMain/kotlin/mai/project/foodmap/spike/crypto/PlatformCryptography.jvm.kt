package mai.project.foodmap.spike.crypto

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.jdk.JDK

internal actual val platformCryptographyProvider: CryptographyProvider
    get() = CryptographyProvider.JDK
