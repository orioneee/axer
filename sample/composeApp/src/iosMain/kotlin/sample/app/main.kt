import androidx.compose.ui.window.ComposeUIViewController
import io.github.orioneee.Axer
import io.github.orioneee.initialize
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController
import sample.app.App
import sample.app.koin.KoinModules


fun MainViewController(): UIViewController {
    Axer.initialize()
    Axer.installAxerErrorHandler()
    startKoin {
        modules(KoinModules.module)
    }
    return ComposeUIViewController { App() }
}