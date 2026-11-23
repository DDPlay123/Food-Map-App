import GoogleMaps
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // 金鑰來自 Secrets.xcconfig 的 MAPS_API_KEY，經由 Info.plist 的 GMSApiKey 讀取
        let apiKey = Bundle.main.object(forInfoDictionaryKey: "GMSApiKey") as? String ?? ""
        GMSServices.provideAPIKey(apiKey)
    }

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}
