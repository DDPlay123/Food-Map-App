# 階段 0：技術驗證（Spike）

這個資料夾放的是跨平台遷移前的技術驗證專案。每個子專案都有自己的 Gradle 設定，**不會被正式 App 的建置引用**，驗證結束後可以整個刪除或保留作為參考。

進度追蹤頁：<https://claude.ai/artifact/8eebaJNurSvzrnV2P8uhYP>

## 前提

後端 API 維持不變。跨平台版本送出的資料（尤其是加密後的密碼）必須和現有 Android App 完全相同。

## crypto-compat：AES 相容性（P0-07、P0-08）

確認 Kotlin Multiplatform 版的加密實作，輸出和現有 `data/src/main/java/mai/project/foodmap/data/utils/AES.kt` 逐位元組一致。

| 檔案 | 用途 |
|---|---|
| `src/commonMain/.../LegacyPasswordCipher.kt` | 跨平台實作，使用 [cryptography-kotlin](https://github.com/whyoleg/cryptography-kotlin) |
| `src/commonTest/.../GoldenVectors.kt` | 以 javax.crypto 參考實作產生的標準答案（測試專用金鑰） |
| `src/commonTest/.../LegacyPasswordCipherTest.kt` | 各平台都會執行：比對標準答案 |
| `src/jvmTest/.../LegacyAesReference.kt` | 現有 `AES.kt` 的 JVM 複本，作為比對基準 |
| `src/jvmTest/.../ReferenceCrossCheckTest.kt` | 只在 JVM 執行：2,000 組隨機輸入與參考實作交叉比對 |

各平台使用的原生加密實作：

- JVM / Android：JDK（javax.crypto）
- iOS：Apple CommonCrypto

### 在 JVM 上執行（任何電腦）

```bash
cd spike/crypto-compat
./gradlew jvmTest
```

如果環境變數 `AES_KEY` 有設定，`realKeyMatchesReferenceWhenAvailable` 會再用正式金鑰比對一次。金鑰不會出現在任何輸出中。

### 在 iOS 模擬器上執行（需要 Mac + Xcode）

```bash
cd spike/crypto-compat
./gradlew iosSimulatorArm64Test
```

通過就代表 iOS 產生的密文和 Android 完全相同，可以直接沿用現有後端。

### 已知的邊界情況

- 含有「落單 surrogate」的字串（不成對的 UTF-16 代理字元），JVM 和 iOS 轉成 UTF-8 時的替代字元不同。一般鍵盤輸入不會產生這種字串，所以不納入測試。
- 解密時，`android.util.Base64` 能接受缺少 `=` padding 的輸入，Kotlin 的 `Base64` 預設不接受。解密只用在 App 自己產生的資料，一定有 padding，所以不影響實際使用。

## map：地圖驗證（P0-02 ～ P0-06）

尚未建立。需要先能在雲端安裝 Android SDK，iOS 部分需要 Mac。
