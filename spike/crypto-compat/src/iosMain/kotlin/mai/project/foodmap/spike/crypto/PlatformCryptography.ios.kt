package mai.project.foodmap.spike.crypto

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.apple.Apple

internal actual val platformCryptographyProvider: CryptographyProvider
    get() = CryptographyProvider.Apple
