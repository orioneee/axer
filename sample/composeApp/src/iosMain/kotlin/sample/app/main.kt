import androidx.compose.ui.window.ComposeUIViewController
import com.oriooneee.axer.Axer
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController
import sample.app.App
import sample.app.koin.KoinModules


fun MainViewController(): UIViewController {
    Axer.installAxerErrorHandler()
    startKoin {
        modules(KoinModules.module)
    }
    return ComposeUIViewController { App() }
}