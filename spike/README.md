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

## map-kmp：地圖驗證（P0-02 ～ P0-06）

確認 Google Maps 在 Compose Multiplatform 的 iOS 上能做到現有地圖頁的所有功能。Google 沒有提供 Compose Multiplatform 版的 Google Maps，所以兩個平台各自用原生 SDK 實作，對外是同一個 `RestaurantMap` Composable。

### 結構

專案結構採用 AGP 9 之後的 KMP 標準：

| 模組 | 內容 |
|---|---|
| `shared` | 共用程式碼與 UI。`commonMain` 放資料模型、Polyline 解碼、示範畫面 `App()` 與 `expect fun RestaurantMap` |
| `shared/androidMain` | 用 Google 官方的 [maps-compose](https://github.com/googlemaps/android-maps-compose) 與群集實作 |
| `shared/iosMain` | 定義 `NativeMapView` 介面，用 `UIKitView` 嵌入 Swift 實作的地圖 |
| `androidApp` | Android 進入點，只套用 `com.android.application` |
| `iosApp` | iOS 進入點（Swift）。`GoogleMapView.swift` 以 `GMSMapView` + `GMUClusterManager` 實作 `NativeMapView` |

iOS 的地圖由 Swift 實作，再透過介面交給 Kotlin，**Kotlin 不直接綁定 Google Maps iOS SDK**（不用 cinterop）。Google Maps SDK 只要在 Xcode 用 Swift Package Manager 加入，Kotlin 的建置流程不受影響。

### 驗證的行為（照搬正式 App 的 MapTabFragment）

- 自訂外觀的餐廳 Marker（店名 + 評分，選取時以品牌色填滿）
- 群集：點群集時，還能放大就縮放到群集範圍；已達最大縮放（例如 6 家座標相同的「美食街」）則列出清單
- 點 Marker：鏡頭移過去，下方橫向列表同步捲到該店
- 橫向列表滑動停止時，鏡頭移到該店
- 「路線」：解碼 Encoded Polyline（與後端格式相同）並畫出
- 深色模式地圖樣式（兩個平台共用同一份 JSON）
- 地圖上疊加 Compose 元件（列表、按鈕、BottomSheet）時的手勢是否互相干擾

### Android

在 `spike/map-kmp/local.properties` 加入 Maps API 金鑰：

```properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=你的金鑰
```

沒有設定 `MAPS_API_KEY` 時，會改讀環境變數 `GOOGLE_API_KEY`。spike 的套件名稱是 `mai.project.foodmap.spike.map`，不會覆蓋手機上的正式 App。如果金鑰限制了套件名稱，需要把這個名稱加進允許清單，或改用一把開發用金鑰。

```bash
cd spike/map-kmp
./gradlew :androidApp:installDebug
```

### iOS（需要 Mac + Xcode）

```bash
brew install xcodegen
```

```bash
cd spike/map-kmp/iosApp
cp Secrets.xcconfig.example Secrets.xcconfig
```

在 `Secrets.xcconfig` 填入 iOS 用的 Maps API 金鑰，然後：

```bash
xcodegen generate
```

```bash
open MapSpike.xcodeproj
```

選擇模擬器或實機執行。建置時會自動呼叫 `./gradlew :shared:embedAndSignAppleFrameworkForXcode` 編譯 Kotlin。

### 目前的驗證狀態

| 項目 | 狀態 |
|---|---|
| Android 建置（`:androidApp:assembleDebug`） | ✅ 通過，無警告 |
| Polyline 解碼測試（`:shared:testAndroidHostTest`） | ✅ 4 項通過 |
| iOS 的 Kotlin 程式碼（`compileKotlinIosArm64`、`compileKotlinIosSimulatorArm64`） | ✅ 在 Linux 上以 klib 交叉編譯通過 |
| iOS framework 連結、Swift 程式碼編譯、實際執行 | ⏳ 需要 Mac |
| Android 在手機上實際執行 | ⏳ 需要 Maps API 金鑰與裝置 |

在 Linux 或 Windows 上，也能先確認 iOS 的 Kotlin 程式碼可以編譯：

```bash
./gradlew :shared:compileKotlinIosSimulatorArm64 -Pkotlin.native.enableKlibsCrossCompilation=true
```

### iOS 驗證清單

| 項目 | 怎麼確認 |
|---|---|
| P0-04 地圖顯示 | 地圖正常載入，能拖曳、縮放、旋轉 |
| P0-05 自訂 Marker | Marker 顯示店名與評分；點選後變成品牌色 |
| P0-04 群集 | 縮小時出現群集數字；點群集會放大；在 101 附近的「美食街」放到最大後再點，會跳出 6 家餐廳的清單 |
| P0-04 路線 | 點「路線」後，畫出台北 101 → 市政府站的路線，鏡頭縮放到整條路線 |
| P0-06 手勢 | 橫向列表可以左右滑，不會帶動地圖；列表停下時地圖移到該店；BottomSheet 可以下拉關閉 |
| 深色模式 | 切換系統深色模式後，地圖樣式跟著改變 |
