import Shared
import SwiftUI
import UIKit

/// 承載 Compose Multiplatform 畫面，並把 Swift 實作的地圖工廠交給 Kotlin。
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(mapFactory: GoogleMapFactory())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
