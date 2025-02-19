# 美食地圖 App

美食地圖是一款便捷的 Android 應用程式，讓你輕鬆查詢鄰近餐廳資訊，並提供最佳路線與高評價餐廳推薦，協助你在茫然中找到理想的用餐地點。
是基於 Google Maps API 實作的台灣美食地圖APP。

後端的Server為 [kkldream/Food-Map-Server](https://github.com/kkldream/Food-Map-Server)

## 主要功能

- **便捷的餐廳查詢**  
  快速查詢附近餐廳資訊，簡單易上手的操作介面，讓使用者能夠迅速找到所需資訊。

- **高評價餐廳推薦**  
  立刻顯示附近評論較高的餐廳，當你不知如何選擇時提供實用的參考建議。

- **最佳路線顯示**  
  自動計算並展示使用者與餐廳間的最佳路徑與距離，助你快速抵達目的地。

- **自定義查詢範圍**  
  根據使用者設定的距離範圍，靈活調整查詢結果，滿足不同需求。

## 架構設計

本專案採用 MVVM 架構，並分為以下模組：

- **App**  
  負責介面顯示，使用 [ViewBinding](https://developer.android.com/topic/libraries/view-binding) 與 [Navigation Component](https://developer.android.com/guide/navigation) 進行開發。

- **Core**  
  實現核心邏輯功能。

- **Data**  
  負責資料來源及 Repository 實現，分為兩個部分：
  - **Network**：利用 [Retrofit](https://square.github.io/retrofit/) 實現網路資料請求。
  - **Local**：使用 [Room](https://developer.android.com/jetpack/androidx/releases/room)、[DataStore](https://developer.android.com/topic/libraries/architecture/datastore) 以及 [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) 進行本地資料存取。

- **Domain**  
  負責定義 Repository 介面與 UI Model，確保資料流動及顯示的一致性。

## 依賴注入 (DI)

本專案使用 [Dagger-Hilt](https://dagger.dev/hilt/) 作為依賴注入工具，提升專案模組間的解耦性。

## 使用的 Google 工具

- [Firebase](https://firebase.google.com/)
- [Google Maps API](https://developers.google.com/maps)

## 第三方套件

- [Timber](https://github.com/JakeWharton/timber)
- [Retrofit](https://square.github.io/retrofit/)
- [Coil](https://coil-kt.github.io/coil/)
- [Lottie](https://github.com/airbnb/lottie-android)
- [Geolib-polyline](https://github.com/utsmannn/geolib)
- [Image-Crop](https://github.com/CanHub/Android-Image-Cropper)

## 環境建構

1. 在 `../app/` 路徑下新增 `google-services.json` 檔案。
2. 在 `local.properties` 檔案中新增以下內容：
   ```properties
   GOOGLE_API_KEY = YOUR_GOOGLE_API_KEY
   BASE_URL = "YOUR_SERVER_HOST"
   AES_KEY = "YOUR_AES_KEY"
   RELEASE_STORE_FILE = YOUR_RELEASE_STORE_FILE
   DEBUG_STORE_FILE = YOUR_DEBUG_STORE_FILE
   STORE_PASSWORD = YOUR_STORE_PASSWORD
   KEY_ALIAS = YOUR_KEY_ALIAS
   KEY_PASSWORD = YOUR_KEY_PASSWORD
