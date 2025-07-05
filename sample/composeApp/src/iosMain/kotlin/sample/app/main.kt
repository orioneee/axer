import androidx.compose.ui.window.ComposeUIViewController
import io.github.orioneee.Axer
import io.github.orioneee.installErrorHandler
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController
import sample.app.App
import sample.app.koin.KoinModules
import kotlin.experimental.ExperimentalNativeApi


@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    Axer.installErrorHandler()
    startKoin {
        modules(KoinModules.module)
    }
    return ComposeUIViewController { App() }
}