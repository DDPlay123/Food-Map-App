package mai.project.foodmap.spike.map

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS 進入點。Swift 端呼叫方式：
 * `MainViewControllerKt.MainViewController(mapFactory: GoogleMapFactory())`
 */
fun MainViewController(mapFactory: NativeMapFactory): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(LocalNativeMapFactory provides mapFactory) {
        App()
    }
}
